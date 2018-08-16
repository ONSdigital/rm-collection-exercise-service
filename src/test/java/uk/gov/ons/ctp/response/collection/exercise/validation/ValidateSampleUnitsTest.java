package uk.gov.ons.ctp.response.collection.exercise.validation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.client.CollectionInstrumentSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.impl.PartySvcRestClientImpl;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.ScheduleSettings;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

/** Tests for the ValidatesSampleTest */
@RunWith(MockitoJUnitRunner.class)
public class ValidateSampleUnitsTest {

  @Mock private DistributedListManager<Integer> sampleValidationListManager;

  @Mock private CollectionExerciseRepository collectRepo;

  @Mock private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  @Mock private ExerciseSampleUnitService sampleUnitSvc;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock private PartySvcRestClientImpl partySvcClient;

  @Mock private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Mock
  @Qualifier("sampleUnitGroup")
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Mock private AppConfig appConfig;

  @Mock private ScheduleSettings scheduleSettings;

  @Mock private CollectionExerciseService collexService;

  @InjectMocks private ValidateSampleUnits validateSampleUnits;

  private static final String VALIDATION_LIST_ID = "group";
  private static final List<Integer> EMPTY_VALIDATION_LIST = new ArrayList<>();

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;

  private static final String COLLECTION_EXERCISE_ID_1 = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String SAMPLE_UNIT_REF = "50000065975";

  private static final Map<String, String> CI_1_SVC_SEARCH =
      ImmutableMap.<String, String>builder()
          .put("RU_REF", SAMPLE_UNIT_REF)
          .put("COLLECTION_EXERCISE", COLLECTION_EXERCISE_ID_1)
          .put("SURVEY_ID", "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87")
          .build();

  private List<CollectionExercise> collectionExercises;
  private List<ExerciseSampleUnit> sampleUnits;
  private List<ExerciseSampleUnitGroup> sampleUnitGroups;
  private List<SurveyClassifierDTO> classifierTypeSelectors;
  private List<SurveyClassifierTypeDTO> classifierTypeSelector;
  private List<CollectionInstrumentDTO> collectionInstruments;
  private List<PartyDTO> parties;

  /**
   * Setup Mock responses when all created and injected into test subject.
   *
   * @throws Exception from FixtureHelper loading test data flat files.
   */
  @Before
  public void setUp() throws Exception {
    when(appConfig.getSchedules()).thenReturn(scheduleSettings);
    when(scheduleSettings.getValidationScheduleRetrievalMax())
        .thenReturn(DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX);

    // Mock data
    collectionExercises = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    sampleUnits = FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);
    sampleUnitGroups = FixtureHelper.loadClassFixtures(ExerciseSampleUnitGroup[].class);
    classifierTypeSelectors = FixtureHelper.loadClassFixtures(SurveyClassifierDTO[].class);
    classifierTypeSelector = FixtureHelper.loadClassFixtures(SurveyClassifierTypeDTO[].class);
    collectionInstruments = FixtureHelper.loadClassFixtures(CollectionInstrumentDTO[].class);
    parties = FixtureHelper.loadClassFixtures(PartyDTO[].class);

    // Given
    when(collexService.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED))
        .thenReturn(collectionExercises);

    // Mock retrieveSampleUnitGroups
    when(sampleValidationListManager.findList(VALIDATION_LIST_ID, false))
        .thenReturn(EMPTY_VALIDATION_LIST);
    when(sampleUnitGroupSvc
            .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.INIT,
                collectionExercises,
                EMPTY_VALIDATION_LIST,
                new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax())))
        .thenReturn(sampleUnitGroups);

    // Mock addCollectionInstrumentIds
    when(sampleUnitSvc.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

    // Mock requestSurveyClassifiers
    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);
    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector.get(0));

    // Mock requestCollectionInstrumentId
    when(collectionInstrumentSvcClient.requestCollectionInstruments(
            new JSONObject(CI_1_SVC_SEARCH).toString()))
        .thenReturn(collectionInstruments.subList(0, 1));
    when(partySvcClient.requestParty(
            sampleUnits.get(0).getSampleUnitType(), sampleUnits.get(0).getSampleUnitRef()))
        .thenReturn(parties.get(0));

    when(sampleUnitGroupState.transition(SampleUnitGroupState.INIT, SampleUnitGroupEvent.VALIDATE))
        .thenReturn(SampleUnitGroupState.VALIDATED);
    when(sampleUnitGroupState.transition(
            SampleUnitGroupState.INIT, SampleUnitGroupEvent.INVALIDATE))
        .thenReturn(SampleUnitGroupState.FAILEDVALIDATION);

    // Mock getCollectionExerciseTransistionState
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.INIT, collectionExercises.get(0)))
        .thenReturn(0L);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, collectionExercises.get(0)))
        .thenReturn(collectionExercises.get(0).getSampleSize().longValue());
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, collectionExercises.get(0)))
        .thenReturn(0L);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.INIT, collectionExercises.get(1)))
        .thenReturn(0L);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, collectionExercises.get(1)))
        .thenReturn(collectionExercises.get(1).getSampleSize().longValue());
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, collectionExercises.get(1)))
        .thenReturn(0L);
  }

  /** Test validate all SampleUnitGroups and CollectionExercises */
  @Test
  public void testValidateSampleUnits() throws Exception {

    // Given setup()

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(1))
        .transitionCollectionExercise(collectionExercises.get(0), CollectionExerciseEvent.VALIDATE);
    verify(collexService, times(1))
        .transitionCollectionExercise(collectionExercises.get(1), CollectionExerciseEvent.VALIDATE);
  }

  /** Test no exerises in EXECUTED state */
  @Test
  public void testValidateSampleUnitsNoExercises() throws Exception {

    // Given
    when(collexService.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED))
        .thenReturn(Collections.EMPTY_LIST);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  /** Test no sample unit groups found for collection exercises */
  @Test
  public void testValidateSampleUnitsNoSampleUnitGroups() throws Exception {

    // Given
    when(sampleUnitGroupSvc
            .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
                SampleUnitGroupDTO.SampleUnitGroupState.INIT,
                collectionExercises,
                EMPTY_VALIDATION_LIST,
                new PageRequest(0, appConfig.getSchedules().getValidationScheduleRetrievalMax())))
        .thenReturn(Collections.EMPTY_LIST);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  /** Test party service throws RestClientException */
  @Test
  public void testValidateSampleUnitsRequestPartyFail() throws Exception {

    // Given
    when(partySvcClient.requestParty(
            sampleUnits.get(0).getSampleUnitType(), sampleUnits.get(0).getSampleUnitRef()))
        .thenThrow(RestClientException.class);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(2))
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  /**
   * Test sampleUnitGroup transition to FAILEDVALIDATION state when not all sample groups are
   * VALIDATED
   */
  @Test
  public void testValidateSampleUnitsInvalidTransition() throws Exception {

    // Given
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, collectionExercises.get(0)))
        .thenReturn(collectionExercises.get(0).getSampleSize().longValue() - 1);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, collectionExercises.get(0)))
        .thenReturn(1L);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.VALIDATED, collectionExercises.get(1)))
        .thenReturn(collectionExercises.get(1).getSampleSize().longValue() - 1);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
            SampleUnitGroupState.FAILEDVALIDATION, collectionExercises.get(1)))
        .thenReturn(1L);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(1))
        .transitionCollectionExercise(
            collectionExercises.get(0), CollectionExerciseEvent.INVALIDATE);
    verify(collexService, times(1))
        .transitionCollectionExercise(
            collectionExercises.get(1), CollectionExerciseEvent.INVALIDATE);
  }

  /** Test transition collection exercise throws CTPException */
  @Test
  public void testValidateSampleUnitsTransitionCTPError() throws Exception {

    // Given
    doThrow(CTPException.class)
        .when(collexService)
        .transitionCollectionExercise(collectionExercises.get(0), CollectionExerciseEvent.VALIDATE);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
  }

  @Test
  public void testValidateSampleUnitsRequestSurveyClassifierTypeSelectorsFail() throws Exception {

    // Given
    when(surveySvcClient.requestClassifierTypeSelectors(any()))
        .thenThrow(RestClientException.class);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsRequestSurveyClassifierTypeSelectorsEmpty() throws Exception {

    // Given
    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(Collections.EMPTY_LIST);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsRequestSurveyClassifierTypeSelectorEmpty() throws Exception {

    // Given
    when(surveySvcClient.requestClassifierTypeSelector(any(), any())).thenReturn(null);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsListLockingException() throws Exception {

    // Given
    when(sampleValidationListManager.findList(VALIDATION_LIST_ID, false))
        .thenThrow(LockingException.class);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, never())
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsFailsValidation() throws Exception {

    // Given one of the sample units will fail validation
    when(partySvcClient.requestParty(
            sampleUnits.get(0).getSampleUnitType(), sampleUnits.get(0).getSampleUnitRef()))
        .thenThrow(RestClientException.class);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(2))
        .transitionCollectionExercise(
            isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsInvalidClassifier() throws Exception {

    // Given one of the sample units will fail validation
    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
      .thenReturn(classifierTypeSelector.get(1));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(2))
      .transitionCollectionExercise(
        isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsNoCollectionInstruments() throws Exception {

    // Given one of the sample units will fail validation
    when(collectionInstrumentSvcClient.requestCollectionInstruments(
      new JSONObject(CI_1_SVC_SEARCH).toString()))
      .thenReturn(Collections.EMPTY_LIST);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(2))
      .transitionCollectionExercise(
        isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }

  @Test
  public void testValidateSampleUnitsMultipleCollectionInstruments() throws Exception {

    // Given one of the sample units will fail validation
    when(collectionInstrumentSvcClient.requestCollectionInstruments(
      new JSONObject(CI_1_SVC_SEARCH).toString()))
      .thenReturn(collectionInstruments);

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(any(), any());
    verify(collexService, times(2))
      .transitionCollectionExercise(
        isA(CollectionExercise.class), isA(CollectionExerciseEvent.class));
  }
}
