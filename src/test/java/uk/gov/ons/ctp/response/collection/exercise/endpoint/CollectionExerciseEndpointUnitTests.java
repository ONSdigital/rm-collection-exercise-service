package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.MvcHelper.postJson;
import static uk.gov.ons.ctp.common.MvcHelper.putJson;
import static uk.gov.ons.ctp.common.TestHelper.createTestDate;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseBeanMapper;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkedSampleSummariesDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

/**
 * Collection Exercise Endpoint Unit tests
 */
@Slf4j
public class CollectionExerciseEndpointUnitTests {
  private static final String LINK_SAMPLE_SUMMARY_JSON = "{\"sampleSummaryIds\": [\"87043936-4d38-4696-952a-fcd55a51be96\", \"cf23b621-c613-424c-9d0d-53a9cfa82f3a\"]}";
  private static final UUID SURVEY_ID = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e");
  private static final int SURVEY_FK = 1;
  private static final UUID COLLECTIONEXERCISE_ID1 = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
  private static final UUID COLLECTIONEXERCISE_ID2 = UUID.fromString("e653d1ce-551b-4b41-b05c-eec02f71891e");
  private static final String COLLECTIONEXERCISE_NAME = ("BRES_2016");
  private static final String COLLECTIONEXERCISE_DATE = createTestDate("2017-05-15T12:00:00.00+0100");
  private static final String COLLECTIONEXERCISE_STATE = ("EXECUTED");
  private static final UUID ACTIONPLANID = UUID.fromString("2de9d435-7d99-4819-9af8-5942f515500b");
  private static final int SAMPLEUNITSTOTAL = 500;

  private static final UUID SURVEY_IDNOTFOUND = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff5e");
  private static final UUID COLLECTIONEXERCISE_IDNOTFOUND = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff6e");

  private static final UUID SAMPLE_SUMARY_ID1 = UUID.fromString("87043936-4d38-4696-952a-fcd55a51be96");
  private static final UUID SAMPLE_SUMARY_ID2 = UUID.fromString("cf23b621-c613-424c-9d0d-53a9cfa82f3a");

  @InjectMocks
  private CollectionExerciseEndpoint colectionExerciseEndpoint;

  @InjectMocks
  private CollectionExerciseExecutionEndpoint collectionExerciseExecutionEndpoint;

  @Mock
  private SampleService sampleService;

  @Mock
  private CollectionExerciseService collectionExerciseService;

  @Mock
  private SurveyService surveyService;

  @Spy
  private MapperFacade mapperFacade = new CollectionExerciseBeanMapper();

  private MockMvc mockCollectionExerciseMvc;
  private MockMvc mockCollectionExerciseExecutionMvc;
  private List<Survey> surveyResults;
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

    this.mockCollectionExerciseMvc = MockMvcBuilders
            .standaloneSetup(colectionExerciseEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();

    this.mockCollectionExerciseExecutionMvc = MockMvcBuilders
        .standaloneSetup(collectionExerciseExecutionEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();

    this.surveyResults = FixtureHelper.loadClassFixtures(Survey[].class);
    this.collectionExerciseResults = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    this.sampleUnitsRequestDTOResults = FixtureHelper.loadClassFixtures(SampleUnitsRequestDTO[].class);
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
    when(surveyService.findSurvey(SURVEY_ID)).thenReturn(surveyResults.get(0));
    when(collectionExerciseService.findCollectionExercisesForSurvey(surveyResults.get(0)))
        .thenReturn(collectionExerciseResults);

    ResultActions actions = mockCollectionExerciseMvc.perform(getJson(String.format("/collectionexercises/survey/%s", SURVEY_ID)));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercisesForSurvey"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].*", hasSize(3)))
        .andExpect(jsonPath("$[*].id",
            containsInAnyOrder(COLLECTIONEXERCISE_ID1.toString(), COLLECTIONEXERCISE_ID2.toString())))
        .andExpect(jsonPath("$[*].name", containsInAnyOrder(COLLECTIONEXERCISE_NAME, COLLECTIONEXERCISE_NAME)))
        .andExpect(
            jsonPath("$[*].scheduledExecutionDateTime", containsInAnyOrder(COLLECTIONEXERCISE_DATE,
                COLLECTIONEXERCISE_DATE)));

  }

  /**
   * Tests collection exercise not found.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExercisesForSurveyNotFound() throws Exception {
    ResultActions actions = mockCollectionExerciseMvc
        .perform(getJson(String.format("/collectionexercises/survey/%s", SURVEY_IDNOTFOUND)));

    actions.andExpect(status().isNotFound())
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
    when(surveyService.findSurveyByFK(SURVEY_FK)).thenReturn(surveyResults.get(0));

    MockHttpServletRequestBuilder json = getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_ID1));

    log.info("json: {}", json);

    ResultActions actions = mockCollectionExerciseMvc.perform(json);

    log.info("actions: {}", actions);

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"))
        .andExpect(jsonPath("$.id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$.surveyId", is(SURVEY_ID.toString())))
        .andExpect(jsonPath("$.name", is(COLLECTIONEXERCISE_NAME)))
        .andExpect(jsonPath("$.state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$.caseTypes[*]", hasSize(1)))
        .andExpect(jsonPath("$.caseTypes[*].*", hasSize(2)))
        .andExpect(jsonPath("$.caseTypes[*].actionPlanId", containsInAnyOrder(ACTIONPLANID.toString())));

  }

  /**
   * Tests collection exercise not found.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findCollectionExerciseNotFound() throws Exception {
    ResultActions actions = mockCollectionExerciseMvc
        .perform(getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_IDNOTFOUND)));

    actions.andExpect(status().isNotFound())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"));

  }

  /**
   * Tests put request returns sampleUnitsTotal.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void requestSampleUnits() throws Exception {
    when(sampleService.requestSampleUnits(COLLECTIONEXERCISE_ID1)).thenReturn(sampleUnitsRequestDTOResults.get(0));

    ResultActions actions = mockCollectionExerciseExecutionMvc
        .perform(postJson(String.format("/collectionexerciseexecution/%s", COLLECTIONEXERCISE_ID1), "{}"));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseExecutionEndpoint.class))
        .andExpect(handler().methodName("requestSampleUnits"))
        .andExpect(jsonPath("$.*", hasSize(1)))
        .andExpect(jsonPath("$.sampleUnitsTotal", is(SAMPLEUNITSTOTAL)));

  }

  /**
   * Test to get all collection exercises.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void findAllCollectionExercises() throws Exception {
    when(collectionExerciseService.findAllCollectionExercise()).thenReturn(collectionExerciseResults);
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.getCaseTypesList(collectionExerciseResults.get(0)))
        .thenReturn(caseTypeDefaultResults);
    when(surveyService.findSurveyByFK(SURVEY_FK)).thenReturn(surveyResults.get(0));

    ResultActions actions = mockCollectionExerciseMvc.perform(getJson(String.format("/collectionexercises/")));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getAllCollectionExercises"))
        .andExpect(jsonPath("$[0].id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$[0].surveyId", is(SURVEY_ID.toString())))
        .andExpect(jsonPath("$[0].name", is(COLLECTIONEXERCISE_NAME)))
        .andExpect(jsonPath("$[0].state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$[0].exerciseRef", is("2017")))
        .andExpect(jsonPath("$[1].id", is(COLLECTIONEXERCISE_ID2.toString())))
        .andExpect(jsonPath("$[1].surveyId", is(SURVEY_ID.toString())))
        .andExpect(jsonPath("$[1].name", is(COLLECTIONEXERCISE_NAME)))
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
    sampleSummaries.add(SAMPLE_SUMARY_ID1);
    sampleSummaries.add(SAMPLE_SUMARY_ID2);

    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.linkSampleSummaryToCollectionExercise(COLLECTIONEXERCISE_ID1, sampleSummaries))
        .thenReturn(sampleLink);

    ResultActions actions = mockCollectionExerciseMvc
        .perform(
            putJson(String.format("/collectionexercises/link/%s", COLLECTIONEXERCISE_ID1), LINK_SAMPLE_SUMMARY_JSON));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("linkSampleSummary"))
        .andExpect(jsonPath("$.collectionExerciseId", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$.sampleSummaryIds[*]", containsInAnyOrder(SAMPLE_SUMARY_ID1.toString(), SAMPLE_SUMARY_ID2.toString())));

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
    when(collectionExerciseService.findLinkedSampleSummaries(COLLECTIONEXERCISE_ID1)).thenReturn(sampleLink);

    ResultActions actions = mockCollectionExerciseMvc
        .perform(getJson(String.format("/collectionexercises/link/%s", COLLECTIONEXERCISE_ID1)));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("requestLinkedSampleSummaries"))
        .andExpect(jsonPath("$[0]", is(SAMPLE_SUMARY_ID1.toString())))
        .andExpect(jsonPath("$[1]", is(SAMPLE_SUMARY_ID2.toString())));

  }

}
