package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static uk.gov.ons.ctp.common.MvcHelper.*;
import static uk.gov.ons.ctp.common.utility.MockMvcControllerAdviceHelper.mockAdviceFor;

/**
 * Collection Exercise Endpoint Unit tests
 */
@Slf4j
public class CollectionExerciseExecutionEndpointUnitTests {
  private static final UUID COLLECTIONEXERCISE_ID1 = UUID.fromString("3ec82e0e-18ff-4886-8703-5b83442041ba");
  private static final int SAMPLEUNITSTOTAL = 500;

  @InjectMocks
  private CollectionExerciseExecutionEndpoint collectionExerciseExecutionEndpoint;

  @Mock
  private SampleService sampleService;

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

    this.mockCollectionExerciseExecutionMvc = MockMvcBuilders
        .standaloneSetup(collectionExerciseExecutionEndpoint)
        .setHandlerExceptionResolvers(mockAdviceFor(RestExceptionHandler.class))
        .setMessageConverters(new MappingJackson2HttpMessageConverter(new CustomObjectMapper()))
        .build();

    this.sampleUnitsRequestDTOResults = FixtureHelper.loadClassFixtures(SampleUnitsRequestDTO[].class);
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

}
