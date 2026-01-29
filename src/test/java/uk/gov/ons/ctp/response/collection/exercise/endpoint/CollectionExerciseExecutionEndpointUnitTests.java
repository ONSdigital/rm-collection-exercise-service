package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.lib.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;
import static uk.gov.ons.ctp.response.collection.exercise.lib.common.MvcHelper.postJson;

import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.lib.common.FixtureHelper;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleSummaryService;

/** Collection Exercise Endpoint Unit tests */
public class CollectionExerciseExecutionEndpointUnitTests {

  @Mock private SampleSummaryService sampleSummaryService;

  private static final Logger log =
      LoggerFactory.getLogger(CollectionExerciseExecutionEndpointUnitTests.class);

  private static final UUID COLLECTIONEXERCISE_ID1 =
      UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
  private static final int SAMPLEUNITSTOTAL = 500;

  @InjectMocks private CollectionExerciseExecutionEndpoint collectionExerciseExecutionEndpoint;

  private MockMvc mockCollectionExerciseExecutionMvc;
  private List<SampleUnitsRequestDTO> sampleUnitsRequestDTOResults;

  /**
   * Set up of tests
   *
   * @throws Exception exception thrown
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    this.mockCollectionExerciseExecutionMvc =
        MockMvcBuilders.standaloneSetup(collectionExerciseExecutionEndpoint)
            .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
            .build();

    this.sampleUnitsRequestDTOResults =
        FixtureHelper.loadClassFixtures(SampleUnitsRequestDTO[].class);
  }

  /**
   * Tests put request returns sampleUnitsTotal.
   *
   * @throws Exception exception thrown
   */
  @Test
  public void requestSampleUnits() throws Exception {

    ResultActions actions =
        mockCollectionExerciseExecutionMvc.perform(
            postJson(
                String.format("/collectionexerciseexecution/%s", COLLECTIONEXERCISE_ID1), "{}"));

    actions
        .andExpect(status().isOk())
        .andExpect(handler().handlerType(CollectionExerciseExecutionEndpoint.class))
        .andExpect(handler().methodName("setReadyForLive"));
  }
}
