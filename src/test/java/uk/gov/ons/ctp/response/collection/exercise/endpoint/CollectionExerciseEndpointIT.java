package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.mashape.unirest.http.HttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.ResponseEventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.utility.PubSubEmulator;

/** A class to contain integration tests for the collection exercise service */
@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WireMockTest(httpPort = 18002)
public class CollectionExerciseEndpointIT {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEndpointIT.class);

  private static final UUID TEST_SURVEY_ID =
      UUID.fromString("c23bb1c1-5202-43bb-8357-7a07c844308f");
  private static final String TEST_USERNAME = "admin";
  private static final String TEST_PASSWORD = "secret";

  @LocalServerPort private int port;

  @Autowired private CollectionExerciseRepository collexRepository;

  @Autowired private ObjectMapper mapper;

  @Autowired private AppConfig appConfig;

  @Autowired private CollectionExerciseRepository collectionExerciseRepository;

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private SupplementaryDatasetRepository supplementaryDatasetRepository;

  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  private CollectionExerciseClient client;
  private PubSubEmulator pubSubEmulator = new PubSubEmulator();

  public CollectionExerciseEndpointIT() throws IOException {}

  @TestConfiguration
  static class MockPubSubConfig {
    @Bean
    public PubSubTemplate pubSubTemplate() {
      return mock(PubSubTemplate.class);
    }
  }

  /** Method to set up integration test */
  @Before
  public void setUp() {

    sampleLinkRepository.deleteAllInBatch();
    eventRepository.deleteAllInBatch();
    supplementaryDatasetRepository.deleteAllInBatch();
    collectionExerciseRepository.deleteAllInBatch();

    client = new CollectionExerciseClient(this.port, TEST_USERNAME, TEST_PASSWORD, this.mapper);
    WireMock.configureFor("localhost", 18002);
  }

  /**
   * Method to test construction of a collection exercise via the API - Create a collection exercise
   * - Get the collection exercise from the returned Location header - Assert the collection
   * exercise fields match those expected
   *
   * @throws CTPException thrown by error creating collection exercise
   */
  @Test
  public void shouldCreateCollectionExercise() throws Exception {
    stubSurveyServiceBusiness();
    String exerciseRef = "899990";
    String userDescription = "Test Description";
    Pair<Integer, String> result =
        this.client.createCollectionExercise(TEST_SURVEY_ID, exerciseRef, userDescription);

    assertEquals(201, (int) result.getLeft());

    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(result.getRight());

    assertEquals(TEST_SURVEY_ID, UUID.fromString(newCollex.getSurveyId()));
    assertEquals(exerciseRef, newCollex.getExerciseRef());
    assertEquals(userDescription, newCollex.getUserDescription());
  }

  @Test
  public void shouldUpdateEventDate() throws IOException, CTPException, InterruptedException {
    stubCollectionInstrumentCount();
    stubSurveyServiceBusiness();
    String exerciseRef = "899990";
    String userDescription = "Test Description";

    final Pair<Integer, String> response =
        client.createCollectionExercise(TEST_SURVEY_ID, exerciseRef, userDescription);
    final CollectionExerciseDTO collectionExercise =
        client.getCollectionExercise(response.getRight());

    final EventDTO mps = createEventDTO(collectionExercise, EventService.Tag.mps, 2);
    createEventDTO(collectionExercise, EventService.Tag.go_live, 3);
    createEventDTO(collectionExercise, EventService.Tag.return_by, 4);
    createEventDTO(collectionExercise, EventService.Tag.exercise_end, 5);

    Date newDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

    mps.setTimestamp(newDate);
    HttpResponse res = client.updateEvent(mps);
    ResponseEventDTO responseEventDTO = (ResponseEventDTO) res.getBody();

    final List<EventDTO> events =
        client
            .getEvents(collectionExercise.getId())
            .stream()
            .filter(eventDTO -> EventService.Tag.mps.hasName(eventDTO.getTag()))
            .collect(Collectors.toList());

    final EventDTO createdEvent = events.get(0);
    assertThat(createdEvent.getTag(), is(EventService.Tag.mps.name()));
    assertThat(
        DateUtils.round(createdEvent.getTimestamp(), Calendar.MINUTE).getTime(),
        is(DateUtils.round(newDate, Calendar.MINUTE).getTime()));
    assertTrue(responseEventDTO.getInfo() == null);
  }

  @Test
  public void shouldTransitionCollectionExerciseToReadyToReviewOnSampleSummaryLink()
      throws Exception {
    // Given
    stubSurveyServiceBusiness();
    stubCollectionInstrumentCount();
    stubGetPartyBySampleUnitRef();
    SampleSummaryDTO sampleSummary = stubSampleSummary();
    UUID collectionExerciseId = createScheduledCollectionExercise();

    // When
    this.client.linkSampleSummary(collectionExerciseId, sampleSummary.getId());

    // Then

    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(collectionExerciseId);
    assertEquals(
        CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW, newCollex.getState());
  }

  @Test
  public void shouldTransitionWhenToldSampleIsReady() throws Exception {
    // Given
    stubSurveyServiceBusiness();
    stubCollectionInstrumentCount();
    stubGetPartyBySampleUnitRef();
    SampleSummaryDTO sampleSummary = stubSampleSummary();
    UUID collectionExerciseId = createScheduledCollectionExercise();
    this.client.linkSampleSummary(collectionExerciseId, sampleSummary.getId());

    // When
    this.client.sampleSummaryReadiness(sampleSummary.getId());

    // Then
    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(collectionExerciseId);
    assertEquals(
        CollectionExerciseDTO.CollectionExerciseState.READY_FOR_REVIEW, newCollex.getState());
  }

  @Test
  public void shouldNotTransitionWhenSampleIsNotReady() throws Exception {
    // Given
    stubSurveyServiceBusiness();
    stubCollectionInstrumentCount();
    stubGetPartyBySampleUnitRef();
    SampleSummaryDTO sampleSummary = stubInitSampleSummary();
    UUID collectionExerciseId = createScheduledCollectionExercise();
    this.client.linkSampleSummary(collectionExerciseId, sampleSummary.getId());

    // When
    this.client.sampleSummaryReadiness(sampleSummary.getId());

    // Then
    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(collectionExerciseId);
    assertEquals(CollectionExerciseDTO.CollectionExerciseState.SCHEDULED, newCollex.getState());
  }

  private EventDTO createEventDTO(
      final CollectionExerciseDTO collectionExercise,
      final EventService.Tag tag,
      final Integer daysInFuture) {
    final EventDTO event = new EventDTO();
    event.setTimestamp(Date.from(Instant.now().plus(daysInFuture, ChronoUnit.DAYS)));
    event.setTag(tag.toString());
    event.setCollectionExerciseId(collectionExercise.getId());

    client.createCollectionExerciseEvent(event);
    return event;
  }

  private String sampleUnitToXmlString(SampleUnit sampleUnit) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(SampleUnit.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(sampleUnit, stringWriter);
    return stringWriter.toString();
  }

  private String getRandomRef() {
    Random r = new Random();
    return String.valueOf(r.nextInt(1_000_000));
  }

  private String loadResourceAsString(Class clazz, String resourceName) throws IOException {
    InputStream is = clazz.getResourceAsStream(resourceName);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, StandardCharsets.UTF_8.name());
    return writer.toString();
  }

  private void createCollectionInstrumentStub() throws IOException {

    // TODO change this resource loading stuff it's weird
    String json =
        loadResourceAsString(
            CollectionExerciseService.class,
            "ValidateSampleUnitsTest.CollectionInstrumentDTO.json");
    stubFor(
        get(urlPathEqualTo("/collection-instrument-api/1.0.2/collectioninstrument"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private SampleSummaryDTO stubSampleSummary() throws IOException {
    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);
    sampleSummary.setId(UUID.randomUUID());
    stubFor(
        get(urlPathEqualTo("/samples/samplesummary/" + sampleSummary.getId()))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(sampleSummary))));
    return sampleSummary;
  }

  private SampleSummaryDTO stubInitSampleSummary() throws IOException {
    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());
    stubFor(
        get(urlPathEqualTo("/samples/samplesummary/" + sampleSummary.getId()))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(sampleSummary))));
    return sampleSummary;
  }

  private SampleSummaryDTO stubSampleSummaryInitThenActive() throws IOException {
    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setState(SampleSummaryDTO.SampleState.INIT);
    sampleSummary.setId(UUID.randomUUID());
    stubFor(
        get(urlPathEqualTo("/samples/samplesummary/" + sampleSummary.getId()))
            .inScenario("INIT then ACTIVE")
            .whenScenarioStateIs(Scenario.STARTED)
            .willSetStateTo("ACTIVE")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(sampleSummary))));
    sampleSummary.setState(SampleSummaryDTO.SampleState.ACTIVE);
    stubFor(
        get(urlPathEqualTo("/samples/samplesummary/" + sampleSummary.getId()))
            .inScenario("INIT then ACTIVE")
            .whenScenarioStateIs("ACTIVE")
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(mapper.writeValueAsString(sampleSummary))));
    return sampleSummary;
  }

  private void stubCollectionInstrumentCount() throws IOException {
    stubFor(
        get(urlPathEqualTo("/collection-instrument-api/1.0.2/collectioninstrument/count"))
            .willReturn(aResponse().withBody("1")));
  }

  private void stubGetPartyBySampleUnitRef() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.PartyDTO.with-associations.json");
    stubFor(
        get(urlPathMatching("/party-api/v1/businesses/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubGetPartyNoAssociations() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.PartyDTO.no-associations.json");
    stubFor(
        get(urlPathMatching("/party-api/v1/businesses/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubGetPartyWithAssociations() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.Supplementary.with-associations.json");
    stubFor(
        get(urlPathMatching("/party-api/v1/businesses/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubGetSupplementaryDataServvice() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SupplementaryDatasetDTO.with-associations.json");
    stubFor(
        get(urlPathMatching("/party-api/v1/businesses/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createSurveyServiceClassifierStubs() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierDTO.json");
    stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"
                    + "/classifiertypeselectors"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierTypeDTO.json");
    stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"
                    + "/classifiertypeselectors/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubGetSurvey() throws IOException {
    createSurveyServiceClassifierStubs();

    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyDTO.social.json");
    stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubSurveyServiceBusiness() throws IOException {
    createSurveyServiceClassifierStubs();

    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyDTO.business.json");
    stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private UUID createScheduledCollectionExercise() throws CTPException, IOException {
    String exerciseRef = getRandomRef();
    String userDescription = "Test Description";
    Pair<Integer, String> result =
        client.createCollectionExercise(TEST_SURVEY_ID, exerciseRef, userDescription);
    String collexId = StringUtils.substringAfterLast(result.getRight(), "/");
    UUID collectionExerciseId = UUID.fromString(collexId);

    Arrays.stream(EventService.Tag.values())
        .filter(EventService.Tag::isMandatory)
        .forEach(
            t -> {
              EventDTO event = new EventDTO();
              event.setCollectionExerciseId(collectionExerciseId);
              event.setTag(t.name());
              event.setTimestamp(new Date());
              client.createCollectionExerciseEvent(event);
            });

    return collectionExerciseId;
  }
}
