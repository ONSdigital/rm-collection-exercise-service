package uk.gov.ons.ctp.response.collection.exercise.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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

/**
 * Tests for the ValidatesSampleTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ValidateSampleUnitsTest {

  /**
   * Java Test Configuration
   */
  @Configuration
  static class TestContext {

    /**
     * Validation bean under test
     *
     * @return ValidateSampleUnits bean under test to be autowired with mocks
     */
    @Bean
    public ValidateSampleUnits validateSampleUnits() {
      return new ValidateSampleUnits();
    }

    @MockBean
    @Qualifier("validation")
    private static DistributedListManager<Integer> sampleValidationListManager;

    @MockBean
    private static CollectionExerciseRepository collectRepo;

    @MockBean
    private static ExerciseSampleUnitGroupService sampleUnitGroupSvc;

    @MockBean
    private static ExerciseSampleUnitService sampleUnitSvc;

    @MockBean
    private static SurveySvcClient surveySvcClient;

    @MockBean
    private static PartySvcRestClientImpl partySvcClient;

    @MockBean
    private static CollectionInstrumentSvcClient collectionInstrumentSvcClient;

    @MockBean
    @Qualifier("collectionExercise")
    private static StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
            collectionExerciseTransitionState;

    @MockBean
    @Qualifier("sampleUnitGroup")
    private static StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupState;

    /**
     * Test Configuration bean
     *
     * @return Appconfig bean for test
     */
    @Bean
    public AppConfig appConfig() {
      AppConfig appConfig = new AppConfig();
      ScheduleSettings schedules = new ScheduleSettings();
      schedules.setValidationScheduleRetrievalMax(DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX);
      appConfig.setSchedules(schedules);
      return appConfig;
    }

  }

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private static final String COLLECTION_EXERCISE_ID_1 = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_EXERCISE_ID_2 = "14fb3e68-4dca-46db-bf49-04b84e07e77d";
  private static final String COLLECTION_INSTR_SVC_SEARCH_STRING = "{\"RU_REF\":\"%s\",\"COLLECTION_EXERCISE\":\"%s\"}";
  private static final String COLLECTION_INSTRUMENT_ID = "5ca1afd6-4d01-4e13-bb73-acae62e2e540";
  private static final String SAMPLE_UNIT_REF = "50000065975";

  private static final ArrayList<String> RESULT_COLLECTION_ID = new ArrayList<>(
      Arrays.asList(COLLECTION_EXERCISE_ID_1, COLLECTION_EXERCISE_ID_2));
  private static final ArrayList<String> PARTY_ID = new ArrayList<>(
      Arrays.asList("4eed610a-39f7-437b-a37d-9de1f905cb39",
          "4625df99-7c20-4610-a18c-2f93daffce2a", "45297c23-763d-46a9-b4e5-c37ff5b4fbe8"));

  @Autowired
  private ValidateSampleUnits validate;

  private List<CollectionExercise> collectionExercises;

  @Captor
  private ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
      .forClass(ExerciseSampleUnitGroup.class);

  @Captor
  private ArgumentCaptor<List<ExerciseSampleUnit>> sampleUnitSave;

  /**
   * Setup Mock responses when all created and injected into test subject.
   *
   * @throws Exception from FixtureHelper loading test data flat files.
   */
  @PostConstruct
  public void initIt() throws Exception {

    // Mock data layer domain objects CollectionExercise, SampleUnitGroup,
    // SampleUnit
    collectionExercises = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    when(TestContext.collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED))
        .thenReturn(collectionExercises);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = FixtureHelper.loadClassFixtures(ExerciseSampleUnitGroup[].class);
    when(TestContext.sampleUnitGroupSvc
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            collectionExercises,
            new ArrayList<Integer>(Arrays.asList(IMPOSSIBLE_ID)),
            new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
                .thenReturn(sampleUnitGroups);
    when(TestContext.sampleUnitGroupSvc.countByStateFKAndCollectionExercise(
        eq(SampleUnitGroupDTO.SampleUnitGroupState.INIT),
        any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(2L);
    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(0L);

    List<ExerciseSampleUnit> sampleUnits = FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);
    when(TestContext.sampleUnitSvc.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

    // Mock client Rest calls
    List<SurveyClassifierDTO> classifierTypeSelectors = FixtureHelper.loadClassFixtures(SurveyClassifierDTO[].class);
    when(TestContext.surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    List<SurveyClassifierTypeDTO> classifierTypeSelector = FixtureHelper
        .loadClassFixtures(SurveyClassifierTypeDTO[].class);
    when(TestContext.surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector.get(0));

    List<PartyDTO> partyJson = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    when(TestContext.partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF))
        .thenReturn(partyJson.get(0));

    List<CollectionInstrumentDTO> collectionInstruments = FixtureHelper
        .loadClassFixtures(CollectionInstrumentDTO[].class);
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        String.format(COLLECTION_INSTR_SVC_SEARCH_STRING, SAMPLE_UNIT_REF, COLLECTION_EXERCISE_ID_1)))
            .thenReturn(collectionInstruments);
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        String.format(COLLECTION_INSTR_SVC_SEARCH_STRING, SAMPLE_UNIT_REF, COLLECTION_EXERCISE_ID_2)))
            .thenReturn(collectionInstruments);

    // Mock transition Managers
    when(TestContext.collectionExerciseTransitionState.transition(CollectionExerciseState.EXECUTED,
        CollectionExerciseEvent.VALIDATE)).thenReturn(CollectionExerciseState.VALIDATED);
    when(TestContext.collectionExerciseTransitionState.transition(CollectionExerciseState.EXECUTED,
        CollectionExerciseEvent.INVALIDATE)).thenReturn(CollectionExerciseState.FAILEDVALIDATION);

    when(TestContext.sampleUnitGroupState.transition(SampleUnitGroupState.INIT, SampleUnitGroupEvent.VALIDATE))
        .thenReturn(SampleUnitGroupState.VALIDATED);
    when(TestContext.sampleUnitGroupState.transition(SampleUnitGroupState.INIT, SampleUnitGroupEvent.INVALIDATE))
        .thenReturn(SampleUnitGroupState.FAILEDVALIDATION);
  }

  /**
   * Test happy path through to validate all SampleUnitGroups and
   * CollectionExercises.
   */
  @Test
  public void validateSampleUnitsOK() {
    validate.validateSampleUnits();

    // Two collectionExercises with two SampleUnitGroups each with one
    // sampleUnit per group. Test data configuration. All read the same
    // sampleUnit instance data
    verify(TestContext.sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
        sampleUnitSave.capture());
    List<List<ExerciseSampleUnit>> savedSampleUnits = sampleUnitSave.getAllValues();
    assertTrue(savedSampleUnits.size() == 4);
    savedSampleUnits.forEach((sampleUnits) -> {
      assertTrue(sampleUnits.stream().filter(item -> item.getSampleUnitType() == SampleUnitType.BI).count() == 2L);
      assertTrue(sampleUnits.stream().filter(item -> item.getSampleUnitType() == SampleUnitType.B).count() == 1L);
      sampleUnits.forEach((unit) -> {
        assertTrue(PARTY_ID.contains(unit.getPartyId().toString()));
        assertEquals(COLLECTION_INSTRUMENT_ID, unit.getCollectionInstrumentId().toString());
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
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(RESULT_COLLECTION_ID.contains(exercise.getId().toString()));
      assertEquals(CollectionExerciseState.VALIDATED, exercise.getState());
    });
  }

  /**
   * Test of survey service client failure to get list of classifier type
   * selectors.
   */
  @Test
  public void validateSampleUnitsNoSurveyClassifierTypeSelectors() {

    // Override happy path scenario to receive error from survey service client
    // when requesting list of classifier type selectors.
    when(TestContext.surveySvcClient.requestClassifierTypeSelectors(any()))
        .thenThrow(new RestClientException("Test failure of Survey service"));

    validate.validateSampleUnits();

    // Survey classifiers should be set-up once for a survey so always there,
    // error should not occur. Without them have no chance to get collection
    // instruments so simply exits validation and tries again on another run.
    // Does not save any updates or change any states.
    verify(TestContext.sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(TestContext.collectRepo, never()).saveAndFlush(any());
  }

  /**
   * Test of survey service client failure to get a classifier type selector.
   */
  @Test
  public void validateSampleUnitsNoSurveyClassifierTypeSelector() {

    // Override happy path scenario to receive error from survey service client
    // when requesting a classifier type selector.
    when(TestContext.surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenThrow(new RestClientException("Test failure of Survey service"));

    validate.validateSampleUnits();

    // Survey classifiers should be set-up once for a survey so always there,
    // error should not occur. Without them have no chance to get collection
    // instruments so simply exits validation and tries again on another run.
    // Does not save any updates or change any states.
    verify(TestContext.sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(TestContext.collectRepo, never()).saveAndFlush(any());
  }

  /**
   * Test of party service client failure.
   */
  @Test
  public void validateSampleUnitsNoParty() {

    // Override happy path scenario to receive error from party service client
    when(TestContext.partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, SAMPLE_UNIT_REF))
        .thenThrow(new RestClientException("Test failure of Party service"));
    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validate.validateSampleUnits();

    verify(TestContext.sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
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
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
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
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        String.format(COLLECTION_INSTR_SVC_SEARCH_STRING, SAMPLE_UNIT_REF, COLLECTION_EXERCISE_ID_1)))
            .thenThrow(new RestClientException("Test failure of Collection Instrument service"));
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        String.format(COLLECTION_INSTR_SVC_SEARCH_STRING, SAMPLE_UNIT_REF, COLLECTION_EXERCISE_ID_2)))
            .thenThrow(new RestClientException("Test failure of Collection Instrument service"));

    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupSvc
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validate.validateSampleUnits();

    verify(TestContext.sampleUnitGroupSvc, times(4)).storeExerciseSampleUnitGroup(sampleUnitGroupSave.capture(),
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
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
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
    when(TestContext.sampleUnitGroupSvc
        .findByStateFKAndCollectionExerciseInAndSampleUnitGroupPKNotInOrderByCreatedDateTimeAsc(
            SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            collectionExercises,
            new ArrayList<Integer>(Arrays.asList(IMPOSSIBLE_ID)),
            new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
                .thenReturn(new ArrayList<ExerciseSampleUnitGroup>());

    validate.validateSampleUnits();

    verify(TestContext.sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(TestContext.collectRepo, never()).saveAndFlush(any());
  }

  /**
   * Test of LockingException thrown by DistributedListManager.
   */
  @Test
  public void validateSampleUnitsDistributedListManagerLockingException() {
    // Override happy path scenario to return a LockingException from
    // DistributedListManager.
    try {
      when(TestContext.sampleValidationListManager.findList(any(String.class), any(boolean.class)))
          .thenThrow(new LockingException("Failed to obtain lock Test"));
    } catch (LockingException ex) {
      // Do nothing with it, actually want to catch it in ValidateSampleUnits
    }

    validate.validateSampleUnits();

    verify(TestContext.sampleUnitGroupSvc, never()).storeExerciseSampleUnitGroup(any(), any());
    verify(TestContext.collectRepo, never()).saveAndFlush(any());
  }
}
