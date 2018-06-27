package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.message.rabbit.Rabbitmq;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageBase;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageListener;
import uk.gov.ons.ctp.common.message.rabbit.SimpleMessageSender;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.collection.exercise.validation.ValidateSampleUnits;
import uk.gov.ons.ctp.response.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** A class to contain integration tests for the collection exercise service */
@Slf4j
@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CollectionExerciseEndpointIT {

  // TODO pull these from config
  private static final UUID TEST_SURVEY_ID =
      UUID.fromString("c23bb1c1-5202-43bb-8357-7a07c844308f");
  private static final String TEST_USERNAME = "admin";
  private static final String TEST_PASSWORD = "secret";

  @LocalServerPort private int port;

  @Autowired private CollectionExerciseRepository collexRepository;

  @Autowired private ObjectMapper mapper;

  private ObjectMapper xmlMapper = new XmlMapper();

  @Autowired private AppConfig appConfig;

  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Rule public WireMockRule wireMockRule = new WireMockRule(options().port(18002));

  private CollectionExerciseClient client;

  /** Method to set up integration test */
  @Before
  public void setUp() throws IOException {
    client = new CollectionExerciseClient(this.port, TEST_USERNAME, TEST_PASSWORD, this.mapper);
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
    log.info("************ Starting shouldCreateCollectionExercise *************");
    createSurveyServiceBusinessStub();
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

  /**
   * Creates a new SimpleMessageSender based on the config in AppConfig
   *
   * @return a new SimpleMessageSender
   */
  private SimpleMessageSender getMessageSender() {
    Rabbitmq config = this.appConfig.getRabbitmq();

    return new SimpleMessageSender(
        config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
  }

  /**
   * Creates a new SimpleMessageListener based on the config in AppConfig
   *
   * @return a new SimpleMessageListener
   */
  private SimpleMessageListener getMessageListener() {
    Rabbitmq config = this.appConfig.getRabbitmq();

    return new SimpleMessageListener(
        config.getHost(), config.getPort(), config.getUsername(), config.getPassword());
  }

  /**
   * Method to test the flow receving a message that a sample upload has finished. - Create a
   * collection exercise - Get the collection exercise - Link the collection exercise to a sample
   * summary with a random sample summary id - Send a message to Sample.SampleUploadFinished.binding
   * key on sample-outbound-exchange - Get the sample links for the collection exercise - Assert the
   * sample link is active (and the sample summary ids match)
   *
   * @throws CTPException throw if errors occur in any of the interactions
   */
  @Test
  public void shouldActivateSampleLink() throws Exception {
    log.info("************ Starting shouldActivateSampleLink *************");
    createSurveyServiceBusinessStub();
    createCollectionInstrumentCountStub();

    String exerciseRef = getRandomRef();
    String userDescription = "Test Description";
    Pair<Integer, String> result =
        this.client.createCollectionExercise(TEST_SURVEY_ID, exerciseRef, userDescription);

    assertEquals(201, (int) result.getLeft());
    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(result.getRight());

    log.info("Collection exercise to link: {}", newCollex);

    UUID sampleSummaryId = UUID.randomUUID();

    final int status = this.client.linkSampleSummary(newCollex.getId(), sampleSummaryId);
    assertEquals(200, status);

    SimpleMessageSender sender = getMessageSender();

    SampleSummaryDTO sampleSummary = new SampleSummaryDTO();
    sampleSummary.setId(sampleSummaryId);

    SimpleMessageListener listener = getMessageListener();
    BlockingQueue<String> queue =
        listener.listen(
            SimpleMessageBase.ExchangeType.Direct,
            "collection-outbound-exchange",
            "SampleLink.Activated.binding");

    // This will cause an exception to be thrown as there is no collection instrument service but
    // this is
    // harmless to our purpose
    sender.sendMessage(
        "sample-outbound-exchange",
        "Sample.SampleUploadFinished.binding",
        this.mapper.writeValueAsString(sampleSummary));

    queue.take();

    List<SampleLinkDTO> links = this.client.getSampleLinks(newCollex.getId());

    assertEquals(1, links.size());

    SampleLinkDTO link = links.get(0);

    assertEquals(sampleSummaryId, UUID.fromString(link.getSampleSummaryId()));
    assertEquals("ACTIVE", link.getState());
  }

  @Test
  public void ensureSampleUnitIdIsPropagatedHereSocial() throws Exception {
    log.info("************ Starting ensureSampleUnitIdIsPropagatedHereSocial *************");
    createSurveyServiceSocialStub();
    SampleUnitParent sampleUnit = ensureSampleUnitIdIsPropagatedHere("H");

    assertNull("Party id must be null", sampleUnit.getPartyId());
  }

  @Test
  public void ensureSampleUnitIdIsPropagatedHereBusiness() throws Exception {
    log.info("************ Starting ensureSampleUnitIdIsPropagatedHereBusiness *************");
    createSurveyServiceBusinessStub();
    createPartyServiceNoAssociationsStub();
    createCollectionInstrumentCountStub();
    SampleUnitParent sampleUnit = ensureSampleUnitIdIsPropagatedHere("B");

    assertNotNull("Party id must be not null", sampleUnit.getPartyId());
  }

  @Test
  public void ensureSampleUnitIdIsPropagatedHereBusinessWithExistingEnrolments() throws Exception {
    log.info(
        "************ Starting ensureSampleUnitIdIsPropagatedHereBusinessWithExistingEnrolments *************");
    createSurveyServiceBusinessStub();
    createPartyServicesWithAssociationsStub();
    createCollectionInstrumentCountStub();
    SampleUnitParent sampleUnit = ensureSampleUnitIdIsPropagatedHere("B");

    assertNotNull("Party id must be not null", sampleUnit.getPartyId());
  }

  private SampleUnitParent ensureSampleUnitIdIsPropagatedHere(String type) throws Exception {
    createCollectionInstrumentStub();

    SampleUnit sampleUnit = new SampleUnit();
    UUID id = UUID.randomUUID();
    SimpleMessageSender sender = getMessageSender();

    CollectionExerciseDTO collex =
        createCollectionExercise(TEST_SURVEY_ID, getRandomRef(), "Test description");

    sampleUnit.setId(id.toString());
    sampleUnit.setSampleUnitRef("LMS0001");
    sampleUnit.setCollectionExerciseId(collex.getId().toString());
    sampleUnit.setFormType("");
    sampleUnit.setSampleUnitType(type);
    // sampleUnit.setSampleAttributes(new SampleUnit.SampleAttributes(new ArrayList<>()));

    setSampleSize(collex, 1);
    setState(collex, CollectionExerciseDTO.CollectionExerciseState.EXECUTION_STARTED);

    SimpleMessageListener listener = getMessageListener();
    BlockingQueue<String> queue =
        listener.listen(
            SimpleMessageBase.ExchangeType.Direct,
            "collection-outbound-exchange",
            "Case.CaseDelivery.binding");

    String xml = sampleUnitToXmlString(sampleUnit);

    log.debug("xml = " + xml);

    sender.sendMessage("sample-outbound-exchange", "Sample.SampleDelivery.binding", xml);

    // The service stubs will exit once the call below times out preventing further debugging of
    // this test in other
    // threads (i.e. you have 2 minutes to debug before the service calls will start to fail)
    //    String message = queue.poll(2, TimeUnit.MINUTES);
    // If you need more than 2 minutes to debug this test, then either change the timeout above or
    // comment that line
    // and uncomment the one below (which gives infinite time).
    String message = queue.take();
    log.debug("message = " + message);
    assertNotNull("Timeout waiting for message to arrive in Case.CaseDelivery", message);

    JAXBContext jaxbContext = JAXBContext.newInstance(SampleUnitParent.class);
    SampleUnitParent sampleUnitParent =
        (SampleUnitParent)
            jaxbContext
                .createUnmarshaller()
                .unmarshal(new ByteArrayInputStream(message.getBytes()));

    assertEquals(id, UUID.fromString(sampleUnitParent.getId()));

    return sampleUnitParent;
  }

  private String sampleUnitToXmlString(SampleUnit sampleUnit) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(SampleUnit.class);
    StringWriter stringWriter = new StringWriter();
    jaxbContext.createMarshaller().marshal(sampleUnit, stringWriter);
    return stringWriter.toString();
  }

  private CollectionExerciseDTO createCollectionExercise(
      UUID surveyID, String exerciseRef, String userDescription) throws CTPException {
    Pair<Integer, String> result =
        this.client.createCollectionExercise(surveyID, exerciseRef, userDescription);

    assertEquals(201, (int) result.getLeft());
    CollectionExerciseDTO newCollex = this.client.getCollectionExercise(result.getRight());

    return newCollex;
  }

  private void setSampleSize(CollectionExerciseDTO collex, int sampleSize) throws Exception {
    CollectionExercise c = collexRepository.findOneById(collex.getId());
    c.setSampleSize(sampleSize);
    collexRepository.saveAndFlush(c);
  }

  private String getRandomRef() {
    Random r = new Random();
    return String.valueOf(r.nextInt(1_000_000));
  }

  private void setState(
      CollectionExerciseDTO collex, CollectionExerciseDTO.CollectionExerciseState state)
      throws Exception {
    CollectionExercise c = collexRepository.findOneById(collex.getId());
    c.setState(state);
    collexRepository.saveAndFlush(c);
  }

  private String loadResourceAsString(Class clazz, String resourceName) throws IOException {
    InputStream is = clazz.getResourceAsStream(resourceName);
    StringWriter writer = new StringWriter();
    IOUtils.copy(is, writer, StandardCharsets.UTF_8.name());
    return writer.toString();
  }

  private void createCollectionInstrumentStub() throws IOException {
    String json =
        loadResourceAsString(
            ValidateSampleUnits.class, "ValidateSampleUnitsTest.CollectionInstrumentDTO.json");
    this.wireMockRule.stubFor(
        get(urlPathEqualTo("/collection-instrument-api/1.0.2/collectioninstrument"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createCollectionInstrumentCountStub() throws IOException {
    this.wireMockRule.stubFor(
        get(urlPathEqualTo("/collection-instrument-api/1.0.2/collectioninstrument/count"))
            .willReturn(aResponse().withBody("1")));
  }

  private void createPartyServiceNoAssociationsStub() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.PartyDTO.no-associations.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching("/party-api/v1/parties/type/B/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createPartyServicesWithAssociationsStub() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.PartyDTO.with-associations.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching("/party-api/v1/parties/type/B/ref/(.*)"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createSurveyServiceClassifierStubs() throws IOException {
    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierDTO.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/classifiertypeselectors"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
    json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyClassifierTypeDTO.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/classifiertypeselectors/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createSurveyServiceSocialStub() throws IOException {
    createSurveyServiceClassifierStubs();

    String json =
        loadResourceAsString(
            CollectionExerciseEndpointIT.class,
            "CollectionExerciseEndpointIT.SurveyDTO.social.json");
    this.wireMockRule.stubFor(
        get(urlPathMatching(
                "/surveys/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(json)));
  }

  private void createSurveyServiceBusinessStub() throws IOException {
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
