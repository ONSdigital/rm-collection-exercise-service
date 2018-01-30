package uk.gov.ons.ctp.response.collection.exercise.validation;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
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
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitGroupService;
import uk.gov.ons.ctp.response.collection.exercise.service.ExerciseSampleUnitService;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitType;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the ValidatesSampleTest
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateSampleUnitsTest {

  @Mock
  private DistributedListManager<Integer> sampleValidationListManager;

  @Mock
  private CollectionExerciseRepository collectRepo;

  @Mock
  private ExerciseSampleUnitGroupService sampleUnitGroupSvc;

  @Mock
  private ExerciseSampleUnitService sampleUnitSvc;

  @Mock
  private SurveySvcClient surveySvcClient;

  @Mock
  private PartySvcRestClientImpl partySvcClient;

  @Mock
  private CollectionInstrumentSvcClient collectionInstrumentSvcClient;

  @Mock
  private StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseTransitionState;

  @Mock
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

  @Mock
  private AppConfig appConfig;

  @Mock
  private ScheduleSettings scheduleSettings;

  @InjectMocks
  private ValidateSampleUnits validateSampleUnits;

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private static final String COLLECTION_EXERCISE_ID_1 = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_EXERCISE_ID_2 = "14fb3e68-4dca-46db-bf49-04b84e07e77d";
  private static final String SAMPLE_UNIT_REF = "50000065975";

  private static final Map<String, String> CI_1_SVC_SEARCH = ImmutableMap.<String, String>builder()
          .put("RU_REF", SAMPLE_UNIT_REF)
          .put("COLLECTION_EXERCISE", COLLECTION_EXERCISE_ID_1)
          .put("SURVEY_ID", "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87")
          .build();
  private static final Map<String, String> CI_2_SVC_SEARCH = ImmutableMap.<String, String>builder()
          .put("RU_REF", SAMPLE_UNIT_REF)
          .put("COLLECTION_EXERCISE", COLLECTION_EXERCISE_ID_2)
          .put("SURVEY_ID", "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87")
          .build();

  private static final List<String> RESULT_COLLECTION_ID =Arrays.asList(COLLECTION_EXERCISE_ID_1, COLLECTION_EXERCISE_ID_2);

  private List<CollectionExercise> collectionExercises;

  @Captor
  private ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave;

  @Captor
  private ArgumentCaptor<List<ExerciseSampleUnit>> sampleUnitSave;

  /**
   * Setup Mock responses when all created and injected into test subject.
   *
   * @throws Exception from FixtureHelper loading test data flat files.
   */
  @Before
  public void setup() throws Exception {
    when(appConfig.getSchedules()).thenReturn(scheduleSettings);
    when(scheduleSettings.getValidationScheduleRetrievalMax()).thenReturn(DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX);

    // Mock data layer domain objects CollectionExercise, SampleUnitGroup,
    // SampleUnit
    collectionExercises = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    when(collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED))
        .thenReturn(collectionExercises);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = FixtureHelper.loadClassFixtures(ExerciseSampleUnitGroup[].class);
    when(sampleUnitGroupSvc
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            collectionExercises,
            Collections.singletonList(IMPOSSIBLE_ID),
            new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
                .thenReturn(sampleUnitGroups);
    when(sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
        eq(SampleUnitGroupDTO.SampleUnitGroupState.INIT),
        any()))
            .thenReturn(0L);
    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(2L);
    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(0L);

    List<ExerciseSampleUnit> sampleUnits = FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);
    when(sampleUnitSvc.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

    // Mock client Rest calls
    List<SurveyClassifierDTO> classifierTypeSelectors = FixtureHelper.loadClassFixtures(SurveyClassifierDTO[].class);
    when(surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    List<SurveyClassifierTypeDTO> classifierTypeSelector = FixtureHelper
        .loadClassFixtures(SurveyClassifierTypeDTO[].class);
    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector.get(0));

    List<PartyDTO> partyJson = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    when(partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF))
        .thenReturn(partyJson.get(0));

    List<CollectionInstrumentDTO> collectionInstruments = FixtureHelper
        .loadClassFixtures(CollectionInstrumentDTO[].class);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(
            new JSONObject(CI_1_SVC_SEARCH).toString()))
            .thenReturn(collectionInstruments);
    when(collectionInstrumentSvcClient.requestCollectionInstruments(
            new JSONObject(CI_2_SVC_SEARCH).toString()))
            .thenReturn(collectionInstruments);

    // Mock transition Managers
    when(collectionExerciseTransitionState.transition(CollectionExerciseState.EXECUTED,
        CollectionExerciseEvent.VALIDATE)).thenReturn(CollectionExerciseState.VALIDATED);
    when(collectionExerciseTransitionState.transition(CollectionExerciseState.EXECUTED,
        CollectionExerciseEvent.INVALIDATE)).thenReturn(CollectionExerciseState.FAILEDVALIDATION);

    when(sampleUnitGroupState.transition(SampleUnitGroupState.INIT, SampleUnitGroupEvent.VALIDATE))
        .thenReturn(SampleUnitGroupState.VALIDATED);
    when(sampleUnitGroupState.transition(SampleUnitGroupState.INIT, SampleUnitGroupEvent.INVALIDATE))
        .thenReturn(SampleUnitGroupState.FAILEDVALIDATION);
  }

  /**
   * Test happy path through to validate all SampleUnitGroups and
   * CollectionExercises.
   */
  @Test
  public void validateSampleUnitsOK() {
    List<String> PARTY_ID = Arrays.asList("4eed610a-39f7-437b-a37d-9de1f905cb39", "4625df99-7c20-4610-a18c-2f93daffce2a",
            "45297c23-763d-46a9-b4e5-c37ff5b4fbe8");

    validateSampleUnits.validateSampleUnits();

    // Two collectionExercises with two SampleUnitGroups each with one
    // sampleUnit per group. Test data configuration. All read the same
    // sampleUnit instance data
    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
        sampleUnitSave.capture());
    List<List<ExerciseSampleUnit>> savedSampleUnits = sampleUnitSave.getAllValues();
    assertTrue(savedSampleUnits.size() == 4);
    savedSampleUnits.forEach((sampleUnits) -> {
      assertTrue(sampleUnits.stream().filter(item -> item.getSampleUnitType() == SampleUnitType.BI).count() == 2L);
      assertTrue(sampleUnits.stream().filter(item -> item.getSampleUnitType() == SampleUnitType.B).count() == 1L);
      sampleUnits.forEach((unit) -> {
        assertTrue(PARTY_ID.contains(unit.getPartyId().toString()));
        assertEquals("5ca1afd6-4d01-4e13-bb73-acae62e2e540", unit.getCollectionInstrumentId().toString());
        assertEquals(SAMPLE_UNIT_REF, unit.getSampleUnitRef());
      });
    });

    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.VALIDATED, group.getStateFK());
      assertEquals("0015", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(exercise.getId().toString()));
      assertEquals(CollectionExerciseState.VALIDATED, exercise.getState());
    });
  }

  /**
   * Test of party service client failure.
   */
  @Test
  public void validateSampleUnitsNoParty() {

    // Override happy path scenario to receive error from party service client
    when(partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF))
        .thenThrow(new RestClientException("Test failure of Party service"));
    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validateSampleUnits.validateSampleUnits();

    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
        sampleUnitSave.capture());
    List<List<ExerciseSampleUnit>> savedSampleUnits = sampleUnitSave.getAllValues();
    assertTrue(savedSampleUnits.size() == 4);
    savedSampleUnits.forEach((sampleUnits) -> {
      assertTrue(sampleUnits.size() == 0);
    });

    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.FAILEDVALIDATION, group.getStateFK());
      assertEquals("0015", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(exercise.getId().toString()));
      assertEquals(CollectionExerciseState.FAILEDVALIDATION, exercise.getState());
    });
  }

  /**
   * Test of collection instrument client service failure.
   */
  @Test
  public void validateSampleUnitsNoCollectionInstrument() {

    // Override happy path scenario to receive error from collection instrument
    // service.
    when(collectionInstrumentSvcClient.requestCollectionInstruments(anyString()))
            .thenThrow(new RestClientException("Test failure of Collection Instrument service"));

    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validateSampleUnits.validateSampleUnits();

    verify(sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
        sampleUnitSave.capture());
    List<List<ExerciseSampleUnit>> savedSampleUnits = sampleUnitSave.getAllValues();
    assertEquals(4, savedSampleUnits.size());
    savedSampleUnits.forEach((sampleUnits) -> assertEquals(0, sampleUnits.size()));

    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.FAILEDVALIDATION, group.getStateFK());
      assertEquals("0015", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(exercise.getId().toString()));
      assertEquals(CollectionExerciseState.FAILEDVALIDATION, exercise.getState());
    });

  }

  /**
   * Test of no sampleUnitGroups in state INIT - none to validate.
   */
  @Test
  public void validateSampleUnitsNoneToValidate() {
    // Override happy path scenario to return any empty list querying for
    // sampleUnitGroups.
    when(sampleUnitGroupSvc
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            collectionExercises,
            Collections.singletonList(IMPOSSIBLE_ID),
            new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
                .thenReturn(new ArrayList<>());

    validateSampleUnits.validateSampleUnits();

    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collectRepo, never()).saveAndFlush(any());
  }

  /**
   * Test of LockingException thrown by DistributedListManager.
   */
  @Test
  public void validateSampleUnitsDistributedListManagerLockingException() throws LockingException {
    when(sampleValidationListManager.findList(any(String.class), any(boolean.class)))
        .thenThrow(new LockingException("Failed to obtain lock Test"));

    validateSampleUnits.validateSampleUnits();

    verify(sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(collectRepo, never()).saveAndFlush(any());
  }

  @Test
  public void testSurveyIdClassifierInRequestToCollectionInstrument() {
    // Given 4 sample units belonging to 2 different sample unit groups

    // When
    validateSampleUnits.validateSampleUnits();

    // Then expect SURVEY_ID in 4 CI requests
    verify(collectionInstrumentSvcClient, times(4))
            .requestCollectionInstruments(new JSONObject(CI_1_SVC_SEARCH).toString());
  }

  @Test
  public void testSurveyClassifierInRequestToCollectionInstrumentWhenClassifierSearchFails() {
    // Given classifier search fails
    when(surveySvcClient.requestClassifierTypeSelector(any(), any()))
            .thenThrow(new RestClientException("Test failure of Survey service"));

    // When
    validateSampleUnits.validateSampleUnits();

    // Then
    Map<String, String> searchString = ImmutableMap.of("SURVEY_ID", "cb0711c3-0ac8-41d3-ae0e-567e5ea1ef87");
    verify(collectionInstrumentSvcClient, times(4))
            .requestCollectionInstruments(new JSONObject(searchString).toString());
  }
}
