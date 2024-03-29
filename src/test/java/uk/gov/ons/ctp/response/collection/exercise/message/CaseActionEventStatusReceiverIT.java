package uk.gov.ons.ctp.response.collection.exercise.message;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.endpoint.CollectionExerciseClient;
import uk.gov.ons.ctp.response.collection.exercise.endpoint.CollectionExerciseEndpointIT;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SupplementaryDatasetRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.utility.PubSubEmulator;

@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:/application-test.yml")
public class CaseActionEventStatusReceiverIT {

  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEndpointIT.class);

  private static final UUID TEST_SURVEY_ID =
      UUID.fromString("c23bb1c1-5202-43bb-8357-7a07c844308f");
  private static final String TEST_USERNAME = "admin";
  private static final String TEST_PASSWORD = "secret";
  private static PubSubEmulator PUBSUBEMULATOR;
  private static final String PUBSUB_TOPIC = "event_status_topic";

  @LocalServerPort private int port;

  @Autowired private ObjectMapper mapper;

  @Autowired private CollectionExerciseRepository collectionExerciseRepository;

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private EventRepository eventRepository;

  @Autowired private SupplementaryDatasetRepository supplementaryDatasetRepository;

  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @ClassRule
  public static WireMockClassRule wireMockRule =
      new WireMockClassRule(options().port(18002).bindAddress("localhost"));

  private CollectionExerciseClient client;

  public CaseActionEventStatusReceiverIT() throws IOException {}

  /** Method to set up integration test */
  @Before
  public void setUp() {
    wireMockRule.resetAll();

    sampleLinkRepository.deleteAllInBatch();
    eventRepository.deleteAllInBatch();
    supplementaryDatasetRepository.deleteAllInBatch();
    collectionExerciseRepository.deleteAllInBatch();

    client = new CollectionExerciseClient(this.port, TEST_USERNAME, TEST_PASSWORD, this.mapper);
  }

  @Test
  public void shouldUpdateEventStatus() throws IOException, CTPException, InterruptedException {
    PUBSUBEMULATOR = new PubSubEmulator();
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
    client.updateEvent(mps);
    Event initialEvent =
        eventRepository.findOneByCollectionExerciseIdAndTag(collectionExercise.getId(), "mps");
    Assert.assertEquals(EventDTO.Status.SCHEDULED, initialEvent.getStatus());
    String eventStatusUpdate =
        String.format(
            "{"
                + "\"collectionExerciseID\": \"%s\","
                + "\"tag\": \"mps\","
                + "\"status\":"
                + "\"PROCESSED\""
                + "}",
            collectionExercise.getId());
    PUBSUBEMULATOR.publishMessage(eventStatusUpdate, PUBSUB_TOPIC);
    Thread.sleep(5000);
    Event finalEvent =
        eventRepository.findOneByCollectionExerciseIdAndTag(collectionExercise.getId(), "mps");
    Assert.assertEquals(EventDTO.Status.PROCESSED, finalEvent.getStatus());
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

  private String loadResourceAsString(Class clazz, String resourceName) throws IOException {
    InputStream is = clazz.getResourceAsStream(resourceName);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, StandardCharsets.UTF_8.name());
    return writer.toString();
  }

  private void stubCollectionInstrumentCount() throws IOException {
    this.wireMockRule.stubFor(
        get(urlPathEqualTo("/collection-instrument-api/1.0.2/collectioninstrument/count"))
            .willReturn(aResponse().withBody("1")));
  }

  private void createSurveyServiceClassifierStubs() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierDTO.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"
                    + "/classifiertypeselectors"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierTypeDTO.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"
                    + "/classifiertypeselectors/"
                    + "([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void stubSurveyServiceBusiness() throws IOException {
    createSurveyServiceClassifierStubs();

    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyDTO.business.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }
}
