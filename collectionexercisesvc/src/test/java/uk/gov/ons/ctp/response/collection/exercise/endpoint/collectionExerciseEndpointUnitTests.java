package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.ons.ctp.common.MvcHelper.getJson;
import static uk.gov.ons.ctp.common.MvcHelper.putJson;
import static uk.gov.ons.ctp.common.TestHelper.createTestDate;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ma.glasnost.orika.MapperFacade;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseBeanMapper;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

public class collectionExerciseEndpointUnitTests {
  private static final UUID SURVEY_ID = UUID.fromString("31ec898e-f370-429a-bca4-eab1045aff4e");
  private static final int SURVEY_FK = 1;
  private static final UUID COLLECTIONEXERCISE_ID1 = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
  private static final UUID COLLECTIONEXERCISE_ID2 = UUID.fromString("e653d1ce-551b-4b41-b05c-eec02f71891e");
  private static final String COLLECTIONEXERCISE_NAME = ("BRES_2016");
  private static final String COLLECTIONEXERCISE_DATE = createTestDate("2017-05-15T12:00:00.00+0100");
  private static final String COLLECTIONEXERCISE_STATE = ("EXECUTED");
  private static final UUID ACTIONPLANID = UUID.fromString("2de9d435-7d99-4819-9af8-5942f515500b");
  private static final int SAMPLEUNITSTOTAL = 500;

  @InjectMocks
  private CollectionExerciseEndpoint colectionExerciseEndpoint;

  @Mock
  private SampleService sampleService;

  @Mock
  private CollectionExerciseService collectionExerciseService;

  @Mock
  private SurveyService surveyService;

  @Spy
  private MapperFacade mapperFacade = new CollectionExerciseBeanMapper();

  private MockMvc mockMvc;
  private List<Survey> surveyResults;
  private List<CollectionExercise> collectionExerciseResults;
  private List<SampleUnitsRequestDTO> sampleUnitsRequestDTOResults;
  private Collection<CaseType> caseTypeDefaultResults;

  /**
   * Set up of tests
   * 
   * @throws Exception exception thrown
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockMvc = MockMvcBuilders
        .standaloneSetup(colectionExerciseEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();

    this.surveyResults = FixtureHelper.loadClassFixtures(Survey[].class);
    this.collectionExerciseResults = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    this.sampleUnitsRequestDTOResults = FixtureHelper.loadClassFixtures(SampleUnitsRequestDTO[].class);
    this.caseTypeDefaultResults = new ArrayList<>();
    List<CaseTypeDefault> defaults = FixtureHelper.loadClassFixtures(CaseTypeDefault[].class);
    for (CaseTypeDefault caseTypeDefault : defaults) {
      caseTypeDefaultResults.add(caseTypeDefault);
    }
  }

  @Test
  public void findCollectionExercisesForSurvey() throws Exception {
    when(surveyService.findSurvey(SURVEY_ID)).thenReturn(surveyResults.get(0));
    when(collectionExerciseService.findCollectionExercisesForSurvey(surveyResults.get(0)))
        .thenReturn(collectionExerciseResults);

    ResultActions actions = mockMvc.perform(getJson(String.format("/collectionexercises/survey/%s", SURVEY_ID)));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercisesForSurvey"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].*", hasSize(3)))
        .andExpect(jsonPath("$[*].id",
            containsInAnyOrder(COLLECTIONEXERCISE_ID1.toString(), COLLECTIONEXERCISE_ID2.toString())))
        .andExpect(jsonPath("$[*].name", containsInAnyOrder(COLLECTIONEXERCISE_NAME, COLLECTIONEXERCISE_NAME)))
        .andExpect(
            jsonPath("$[*].scheduledExecution", containsInAnyOrder(COLLECTIONEXERCISE_DATE, COLLECTIONEXERCISE_DATE)));

  }

  @Test
  public void findCollectionExercise() throws Exception {
    when(collectionExerciseService.findCollectionExercise(COLLECTIONEXERCISE_ID1))
        .thenReturn(collectionExerciseResults.get(0));
    when(collectionExerciseService.getCaseTypesList(collectionExerciseResults.get(0)))
        .thenReturn(caseTypeDefaultResults);
    when(surveyService.findSurveyByFK(SURVEY_FK)).thenReturn(surveyResults.get(0));

    ResultActions actions = mockMvc.perform(getJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_ID1)));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("getCollectionExercise"))
        .andExpect(jsonPath("$.*", hasSize(14)))
        .andExpect(jsonPath("$.id", is(COLLECTIONEXERCISE_ID1.toString())))
        .andExpect(jsonPath("$.surveyId", is(SURVEY_ID.toString())))
        .andExpect(jsonPath("$.name", is(COLLECTIONEXERCISE_NAME)))
        .andExpect(jsonPath("$.state", is(COLLECTIONEXERCISE_STATE)))
        .andExpect(jsonPath("$.caseTypes[*]", hasSize(1)))
        .andExpect(jsonPath("$.caseTypes[*].*", hasSize(2)))
        .andExpect(jsonPath("$.caseTypes[*].actionPlanId", containsInAnyOrder(ACTIONPLANID.toString())));

  }

  @Test
  public void requestSampleUnits() throws Exception {
    when(sampleService.requestSampleUnits(COLLECTIONEXERCISE_ID1)).thenReturn(sampleUnitsRequestDTOResults.get(0));

    ResultActions actions = mockMvc
        .perform(putJson(String.format("/collectionexercises/%s", COLLECTIONEXERCISE_ID1), "{}"));

    actions.andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseEndpoint.class))
        .andExpect(handler().methodName("requestSampleUnits"))
        .andExpect(jsonPath("$.*", hasSize(1)))
        .andExpect(jsonPath("$.sampleUnitsTotal", is(SAMPLEUNITSTOTAL)));

  }

}
