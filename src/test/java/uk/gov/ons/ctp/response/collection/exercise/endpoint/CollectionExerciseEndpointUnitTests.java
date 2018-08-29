package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.MvcHelper.putJson;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import ma.glasnost.orika.MapperFacade;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.common.matcher.DateMatcher;
import uk.gov.ons.ctp.common.util.MultiIsoDateFormat;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseBeanMapper;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkedSampleSummariesDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.party.representation.SampleLinkDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Collection Exercise Endpoint Unit tests */
public class CollectionExerciseEndpointUnitTests {
  private static final Logger log =
      LoggerFactory.getLogger(CollectionExerciseEndpointUnitTests.class);

  private static final String LINK_SAMPLE_SUMMARY_JSON =
      "{\"sampleSummaryIds\": [\"87043936-4d38-4696-952a-fcd55a51be96\", \"cf23b621-c613-424c-9d0d-53a9cfa82f3a\"]}";
  private static final UUID SURVEY_ID_1 = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e");
  private static final UUID SURVEY_ID_2 = UUID.fromString("32ec898e-f370-429a-bca4-eab1045aff4e");
  private static final String SURVEY_REF_1 = "221";
  private static final UUID COLLECTIONEXERCISE_ID1 =
      UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
  private static final UUID COLLECTIONEXERCISE_ID2 =
      UUID.fromString("e653d1ce-551b-4b41-b05c-eec02f71891e");
  private static final String COLLECTIONEXERCISE_DATE_OUTPUT = "2017-05-15T12:00:00.000+01:00";

  private static final String COLLECTIONEXERCISE_STATE = ("EXECUTED");
  private static final UUID ACTIONPLANID = UUID.fromString("2de9d435-7d99-4819-9af8-5942f515500b");

  private static final UUID SURVEY_IDNOTFOUND =
      UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff5e");
  private static final UUID COLLECTIONEXERCISE_IDNOTFOUND =
      UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff6e");
  private static final UUID Party_IDNOTFOUND =
      UUID.fromString("cc6bdbfa2-24a8-4317-83c8-5ec7638b0983");

  private static final UUID SAMPLE_SUMMARY_ID1 =
      UUID.fromString("87043936-4d38-4696-952a-fcd55a51be96");
  private static final UUID SAMPLE_SUMMARY_ID2 =
      UUID.fromString("cf23b621-c613-424c-9d0d-53a9cfa82f3a");

  private static final UUID PARTY_ID_1 = UUID.fromString("cc6bdbfa2-24a8-4317-83c8-5ec7638b0983");

  @InjectMocks private CollectionExerciseEndpoint colectionExerciseEndpoint;

  @Mock private SampleService sampleService;

  @Mock private PartySvcClient partySvcClient;

  @Mock private CollectionExerciseService collectionExerciseService;

  @Mock private SurveySvcClient surveyService;

  @Mock private EventService eventService;

  @Spy private MapperFacade mapperFacade = new CollectionExerciseBeanMapper();

  private MockMvc mockCollectionExerciseMvc;
  private MockMvc textPlainMock;
  private List<SurveyDTO> surveyDtoResults;
  private List<CollectionExercise> collectionExerciseResults;
  private List<SampleUnitsRequestDTO> sampleUnitsRequestDTOResults;
  private Collection<CaseType> caseTypeDefaultResults;
  private List<LinkedSampleSummariesDTO> linkedSampleSummaries;
  private List<SampleLink> sampleLink;

  /**
   * Set up of tests
   *
   * @throws Exception exception thrown
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockCollectionExerciseMvc =
        MockMvcBuilders.standaloneSetup(colectionExerciseEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();

    this.textPlainMock =
        MockMvcBuilders.standaloneSetup(colectionExerciseEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .build();

    this.surveyDtoResults = FixtureHelper.loadClassFixtures(SurveyDTO[].class);
    this.collectionExerciseResults = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    this.sampleUnitsRequestDTOResults =
        FixtureHelper.loadClassFixtures(SampleUnitsRequestDTO[].class);
    this.linkedSampleSummaries = FixtureHelper.loadClassFixtures(LinkedSampleSummariesDTO[].class);
    this.sampleLink = FixtureHelper.loadClassFixtures(SampleLink[].class);
    this.caseTypeDefaultResults = new ArrayList<>();
    List<CaseTypeDefault> defaults = FixtureHelper.loadClassFixtures(CaseTypeDefault[].class);
    for (CaseTypeDefault caseTypeDefault : defaults) {
      caseTypeDefaultResults.add(caseTypeDefault);
    }
  }

  /**
   * Tests if collection exercise found for survey.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExercisesForSurvey() throws Exception {
    when(surveyService.findSurvey(SURVEY_ID_1)).thenReturn(surveyDtoResults.get(0));
    when(collectionExerciseService.findCollectionExercisesForSurvey(surveyDtoResults.get(0)))
        .thenReturn(collectionExerciseResults);

    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            getJson(String.format("/collectionexercises/survey/%s", SURVEY_ID_1)));

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercisesForSurvey"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(
            jsonPath(
                "$[*].id",
                containsInAnyOrder(
                    COLLECTIONEXERCISE_ID1.toString(), COLLECTIONEXERCISE_ID2.toString())))
        .andExpect(
            jsonPath(
                "$[*].scheduledExecutionDateTime",
                containsInAnyOrder(
                    new DateMatcher(COLLECTIONEXERCISE_DATE_OUTPUT),
                    new DateMatcher(COLLECTIONEXERCISE_DATE_OUTPUT))));
  }

  /**
   * Tests collection exercise not found.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExercisesForSurveyNotFound() throws Exception {
    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            getJson(String.format("/collectionexercises/survey/%s", SURVEY_IDNOTFOUND)));

    actions
        .andExpect(status().isNotFound())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercisesForSurvey"))
        .andExpect(jsonPath("$.error.code", Is.is(CTPException.Fault.RESOURCE_NOT_FOUND.name())));
  }

  /**
   * Tests if collection exercise found for Id.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExercise() throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.getCaseTypesList(collectionExerciseResults.get(0)))
        .thenReturn(caseTypeDefaultResults);
    when(surveyService.findSurvey(UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e")))
        .thenReturn(surveyDtoResults.get(0));

    MockHttpServletRequestBuilder json =
        getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_ID1));

    log.info("json: {}", json);

    ResultActions actions = mockCollectionExerciseMvc.perform(json);

    log.info("actions: {}", actions);

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"))
        .andExpect(jsonPath("$.id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$.surveyId", is(SURVEY_ID_1.toString())))
        .andExpect(jsonPath("$.state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$.caseTypes[*]", hasSize(1)))
        .andExpect(jsonPath("$.caseTypes[*].*", hasSize(2)))
        .andExpect(
            jsonPath("$.caseTypes[*].actionPlanId", containsInAnyOrder(ACTIONPLANID.toString())));
  }

  /**
   * Tests collection exercise not found.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExerciseNotFound() throws Exception {
    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_IDNOTFOUND)));

    actions
        .andExpect(status().isNotFound())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"));
  }

  /**
   * Test to get collection exercise with get events throws CTP Exception.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void
      findCollectionExerciseGetEventsThrowsCTPExceptionSuccessfullyRetrievesCollectionExercise()
          throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.getCaseTypesList(collectionExerciseResults.get(0)))
        .thenReturn(caseTypeDefaultResults);
    when(surveyService.findSurvey(UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e")))
        .thenReturn(surveyDtoResults.get(0));
    when(eventService.getEvents(COLLECTIONEXERCISE_ID1))
        .thenThrow(new CTPException(CTPException.Fault.SYSTEM_ERROR));

    MockHttpServletRequestBuilder json =
        getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_ID1));

    ResultActions actions = mockCollectionExerciseMvc.perform(json);

    actions
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"))
        .andExpect(jsonPath("$.id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$.surveyId", is(SURVEY_ID_1.toString())))
        .andExpect(jsonPath("$.state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$.caseTypes[*]", hasSize(1)))
        .andExpect(jsonPath("$.caseTypes[*].*", hasSize(2)))
        .andExpect(
            jsonPath("$.caseTypes[*].actionPlanId", containsInAnyOrder(ACTIONPLANID.toString())));
  }

  /**
   * Test to get all collection exercises.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findAllCollectionExercises() throws Exception {
    when(collectionExerciseService.findAllCollectionExercise())
        .thenReturn(collectionExerciseResults);
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.getCaseTypesList(collectionExerciseResults.get(0)))
        .thenReturn(caseTypeDefaultResults);
    when(surveyService.findSurvey(SURVEY_ID_1)).thenReturn(surveyDtoResults.get(0));
    when(surveyService.findSurvey(SURVEY_ID_2)).thenReturn(surveyDtoResults.get(1));

    ResultActions actions =
        mockCollectionExerciseMvc.perform(getJson(String.format("/collectionexercises/")));

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getAllCollectionExercises"))
        .andExpect(jsonPath("$[0].id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$[0].surveyId", is(SURVEY_ID_1.toString())))
        .andExpect(jsonPath("$[0].state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$[0].exerciseRef", is("2017")))
        .andExpect(jsonPath("$[1].id", is(COLLECTIONEXERCISE_ID2.toString())))
        .andExpect(jsonPath("$[1].surveyId", is(SURVEY_ID_2.toString())))
        .andExpect(jsonPath("$[1].state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$[1].exerciseRef", is("2017")));
  }

  /**
   * Tests put request to link sample units.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void linkSampleUnitsPut() throws Exception {
    List<UUID> sampleSummaries = new ArrayList<>();
    sampleSummaries.add(SAMPLE_SUMMARY_ID1);
    sampleSummaries.add(SAMPLE_SUMMARY_ID2);
    SampleLinkDTO sampleLinkDTO1 =
        new SampleLinkDTO(SAMPLE_SUMMARY_ID1.toString(), COLLECTIONEXERCISE_ID1.toString());
    SampleLinkDTO sampleLinkDTO2 =
        new SampleLinkDTO(SAMPLE_SUMMARY_ID2.toString(), COLLECTIONEXERCISE_ID1.toString());

    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.linkSampleSummaryToCollectionExercise(
            COLLECTIONEXERCISE_ID1, sampleSummaries))
        .thenReturn(sampleLink);

    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            putJson(
                String.format("/collectionexercises/link/%s", COLLECTIONEXERCISE_ID1),
                LINK_SAMPLE_SUMMARY_JSON));

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("linkSampleSummary"))
        .andExpect(jsonPath("$.collectionExerciseId", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(
            jsonPath(
                "$.sampleSummaryIds[*]",
                containsInAnyOrder(SAMPLE_SUMMARY_ID1.toString(), SAMPLE_SUMMARY_ID2.toString())));
  }

  /**
   * Tests request to unlink sample.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void testUnlinkSampleUnits() throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));

    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            delete(
                String.format(
                    "/collectionexercises/unlink/%s/sample/%s",
                    COLLECTIONEXERCISE_ID1, SAMPLE_SUMMARY_ID1)));

    actions
        .andExpect(status().isNoContent())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("unlinkSampleSummary"));

    verify(collectionExerciseService, times(1))
        .removeSampleSummaryLink(SAMPLE_SUMMARY_ID1, COLLECTIONEXERCISE_ID1);
  }

  /**
   * Tests request to unlink sample with unknown collection exercise
   *
   * @throws Exception exception thrown
   */
  @Test
  public void testUnlinkSampleUnitsNotFound() throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1)).thenReturn(null);

    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            delete(
                String.format(
                    "/collectionexercises/unlink/%s/sample/%s",
                    COLLECTIONEXERCISE_ID1, SAMPLE_SUMMARY_ID1)));

    actions
        .andExpect(status().isNotFound())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("unlinkSampleSummary"));

    verify(collectionExerciseService, times(0))
        .removeSampleSummaryLink(SAMPLE_SUMMARY_ID1, COLLECTIONEXERCISE_ID1);
  }

  /**
   * Tests to get a list of UUIDs linked to a collection exercise
   *
   * @throws Exception exception thrown
   */
  @Test
  public void getLinkedSampleSummaries() throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.findLinkedSampleSummaries(COLLECTIONEXERCISE_ID1))
        .thenReturn(sampleLink);

    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            getJson(String.format("/collectionexercises/link/%s", COLLECTIONEXERCISE_ID1)));

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("requestLinkedSampleSummaries"))
        .andExpect(jsonPath("$[0]", is(SAMPLE_SUMMARY_ID1.toString())))
        .andExpect(jsonPath("$[1]", is(SAMPLE_SUMMARY_ID2.toString())));
  }

  private String getResourceAsString(String resourceName) throws IOException {
    return new String(
        Files.readAllBytes(Paths.get(getClass().getResource(resourceName).getFile())));
  }

  @Test
  public void testCreateCollectionExerciseMissingSurvey() throws Exception {
    String json =
        getResourceAsString(
            "CollectionExerciseEndpointUnitTests.CollectionExerciseDTO.post-missing-survey.json");
    ResultActions actions =
        mockCollectionExerciseMvc.perform(postJson("/collectionexercises", json));

    actions.andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateCollectionExercise() throws Exception {
    CollectionExercise created =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class, "post").get(0);
    when(surveyService.findSurvey(SURVEY_ID_1)).thenReturn(surveyDtoResults.get(0));
    when(collectionExerciseService.createCollectionExercise(any(), any())).thenReturn(created);

    String json =
        getResourceAsString("CollectionExerciseEndpointUnitTests.CollectionExerciseDTO.post.json");
    ResultActions actions =
        mockCollectionExerciseMvc.perform(postJson("/collectionexercises", json));

    actions
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string(
                    "location",
                    "http://localhost/collectionexercises/3ec82e0e-18ff-4886-8703-5b83442041ba"));
  }

  @Test
  public void testCreateCollectionExerciseBySurveyRef() throws Exception {
    CollectionExercise created =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class, "post").get(0);
    when(surveyService.findSurveyByRef(SURVEY_REF_1)).thenReturn(surveyDtoResults.get(0));
    when(collectionExerciseService.createCollectionExercise(any(), any())).thenReturn(created);

    String json =
        getResourceAsString(
            "CollectionExerciseEndpointUnitTests.CollectionExerciseDTO.post-survey-ref.json");
    ResultActions actions =
        mockCollectionExerciseMvc.perform(postJson("/collectionexercises", json));

    actions
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string(
                    "location",
                    "http://localhost/collectionexercises/3ec82e0e-18ff-4886-8703-5b83442041ba"));
  }

  @Test
  public void testCreateCollectionExerciseAlreadyExists() throws Exception {
    CollectionExercise created =
        FixtureHelper.loadClassFixtures(CollectionExercise[].class, "post").get(0);
    when(surveyService.findSurvey(SURVEY_ID_1)).thenReturn(surveyDtoResults.get(0));
    when(collectionExerciseService.createCollectionExercise(any(), any())).thenReturn(created);
    when(this.collectionExerciseService.findCollectionExercise("202103", surveyDtoResults.get(0)))
        .thenReturn(created);

    String json =
        getResourceAsString("CollectionExerciseEndpointUnitTests.CollectionExerciseDTO.post.json");
    ResultActions actions =
        mockCollectionExerciseMvc.perform(postJson("/collectionexercises", json));

    actions.andExpect(status().isConflict());
  }

  @Test
  public void testUpdateCollectionExercise() throws Exception {
    String json =
        getResourceAsString("CollectionExerciseEndpointUnitTests.CollectionExerciseDTO.post.json");
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    ResultActions actions =
        mockCollectionExerciseMvc.perform(
            putJson(String.format("/collectionexercises/%s", uuid.toString()), json));

    actions.andExpect(status().isOk());

    ArgumentCaptor<CollectionExerciseDTO> dtoCaptor =
        ArgumentCaptor.forClass(CollectionExerciseDTO.class);
    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(this.collectionExerciseService)
        .updateCollectionExercise(uuidCaptor.capture(), dtoCaptor.capture());
    assertEquals(uuid, uuidCaptor.getValue());
    CollectionExerciseDTO dtoArg = dtoCaptor.getValue();
    assertEquals("31ec898e-f370-429a-bca4-eab1045aff4e", dtoArg.getSurveyId());
    assertEquals("202103", dtoArg.getExerciseRef());
    assertEquals("March 2021", dtoArg.getUserDescription());
  }

  @Test
  public void testPatchCollectionExerciseExerciseRef() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    String newExerciseRef = "299909";
    MockHttpServletRequestBuilder builder =
        put(String.format("/collectionexercises/%s/exerciseRef", uuid.toString()), new Object[0])
            .content(newExerciseRef)
            .contentType(MediaType.TEXT_PLAIN);

    ResultActions actions = this.textPlainMock.perform(builder);

    actions.andExpect(status().isOk());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<CollectionExerciseDTO> dtoCaptor =
        ArgumentCaptor.forClass(CollectionExerciseDTO.class);
    verify(this.collectionExerciseService)
        .patchCollectionExercise(uuidCaptor.capture(), dtoCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
    CollectionExerciseDTO collexDto = dtoCaptor.getValue();
    assertEquals(newExerciseRef, collexDto.getExerciseRef());
    assertNull(collexDto.getName());
    assertNull(collexDto.getUserDescription());
    assertNull(collexDto.getSurveyId());
  }

  @Test
  public void testPatchCollectionExerciseName() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    String newName = "New Collex Name";
    MockHttpServletRequestBuilder builder =
        put(String.format("/collectionexercises/%s/name", uuid.toString()), new Object[0])
            .content(newName)
            .contentType(MediaType.TEXT_PLAIN);

    ResultActions actions = this.textPlainMock.perform(builder);

    actions.andExpect(status().isOk());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<CollectionExerciseDTO> dtoCaptor =
        ArgumentCaptor.forClass(CollectionExerciseDTO.class);
    verify(this.collectionExerciseService)
        .patchCollectionExercise(uuidCaptor.capture(), dtoCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
    CollectionExerciseDTO collexDto = dtoCaptor.getValue();
    assertEquals(newName, collexDto.getName());
    assertNull(collexDto.getExerciseRef());
    assertNull(collexDto.getUserDescription());
    assertNull(collexDto.getSurveyId());
  }

  @Test
  public void testPatchCollectionExerciseUserDescription() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    String newUserDesc = "Collection exercise description";
    MockHttpServletRequestBuilder builder =
        put(
                String.format("/collectionexercises/%s/userDescription", uuid.toString()),
                new Object[0])
            .content(newUserDesc)
            .contentType(MediaType.TEXT_PLAIN);

    ResultActions actions = this.textPlainMock.perform(builder);

    actions.andExpect(status().isOk());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<CollectionExerciseDTO> dtoCaptor =
        ArgumentCaptor.forClass(CollectionExerciseDTO.class);
    verify(this.collectionExerciseService)
        .patchCollectionExercise(uuidCaptor.capture(), dtoCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
    CollectionExerciseDTO collexDto = dtoCaptor.getValue();
    assertEquals(newUserDesc, collexDto.getUserDescription());
    assertNull(collexDto.getName());
    assertNull(collexDto.getExerciseRef());
    assertNull(collexDto.getSurveyId());
  }

  @Test
  public void testPatchCollectionExerciseSurveyId() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    String newSurveyId = "4cacb120-3bed-430f-90fd-dddc6256f856";
    MockHttpServletRequestBuilder builder =
        put(String.format("/collectionexercises/%s/surveyId", uuid.toString()), new Object[0])
            .content(newSurveyId)
            .contentType(MediaType.TEXT_PLAIN);

    ResultActions actions = this.textPlainMock.perform(builder);

    actions.andExpect(status().isOk());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<CollectionExerciseDTO> dtoCaptor =
        ArgumentCaptor.forClass(CollectionExerciseDTO.class);
    verify(this.collectionExerciseService)
        .patchCollectionExercise(uuidCaptor.capture(), dtoCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
    CollectionExerciseDTO collexDto = dtoCaptor.getValue();
    assertEquals(newSurveyId, collexDto.getSurveyId());
    assertNull(collexDto.getName());
    assertNull(collexDto.getUserDescription());
    assertNull(collexDto.getExerciseRef());
  }

  @Test
  public void testDeleteCollectionExercise() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.delete(String.format("/collectionexercises/%s", uuid.toString()));
    ResultActions actions = mockCollectionExerciseMvc.perform(builder);

    // NOTE: delete currently returns 202 Accepted as, while the delete request is logged, no delete
    // function has been
    // implemented
    actions.andExpect(status().isAccepted());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(this.collectionExerciseService).deleteCollectionExercise(uuidCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
  }

  /**
   * Test to update collection exercise event timestamp(date) linked to a collection exercise
   *
   * @throws Exception exception thrown
   */
  @Test
  public void testUpdateEvent() throws Exception {

    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    String newDate = "2017-10-07T00:00:00.000+0100";
    MockHttpServletRequestBuilder builder =
        put(String.format("/collectionexercises/%s/events/End", uuid.toString()), new Object[0])
            .content(newDate)
            .contentType(MediaType.TEXT_PLAIN);

    ResultActions actions = this.textPlainMock.perform(builder);

    actions.andExpect(status().isNoContent());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

    MultiIsoDateFormat dateParser = new MultiIsoDateFormat();
    Date expectedDate = dateParser.parse(newDate);

    verify(this.eventService)
        .updateEvent(uuidCaptor.capture(), tagCaptor.capture(), dateCaptor.capture());

    assertEquals(uuid, uuidCaptor.getValue());
    assertEquals("End", tagCaptor.getValue());
    assertEquals(expectedDate, dateCaptor.getValue());
  }

  /**
   * Test to get exercise event linked to a collection exercise
   *
   * @throws Exception exception thrown
   */
  @Test
  public void testGetEvent() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.get(
            String.format("/collectionexercises/%s/events/End", uuid.toString()));
    ResultActions actions = mockCollectionExerciseMvc.perform(builder);
    actions.andExpect(status().isOk());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);

    verify(this.eventService).getEvent(uuidCaptor.capture(), tagCaptor.capture());
  }

  /**
   * Test to delete collection exercise event linked to a collection exercise
   *
   * @throws Exception exception thrown
   */
  @Test
  public void testDeleteEvent() throws Exception {
    UUID uuid = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
    MockHttpServletRequestBuilder builder =
        MockMvcRequestBuilders.delete(
            String.format("/collectionexercises/%s/events/End", uuid.toString()));

    ResultActions actions = mockCollectionExerciseMvc.perform(builder);
    actions.andExpect(status().isNoContent());

    ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
    ArgumentCaptor<String> tagCaptor = ArgumentCaptor.forClass(String.class);

    verify(this.eventService).deleteEvent(uuidCaptor.capture(), tagCaptor.capture());
    assertEquals(uuid, uuidCaptor.getValue());
    assertEquals("End", tagCaptor.getValue());
  }
}
