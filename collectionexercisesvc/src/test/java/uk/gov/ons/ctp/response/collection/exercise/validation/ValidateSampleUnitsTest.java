package uk.gov.ons.ctp.response.collection.exercise.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestClientException;

import uk.gov.ons.ctp.common.FixtureHelper;
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
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.PartyDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.instrument.representation.CollectionInstrumentDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO;
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
    private static CollectionExerciseRepository collectRepo;

    @MockBean
    private static SampleUnitGroupRepository sampleUnitGroupRepo;

    @MockBean
    private static SampleUnitRepository sampleUnitRepo;

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
  }

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;

  private ArrayList<String> resultCollectionId = new ArrayList<String>(
      Arrays.asList("14fb3e68-4dca-46db-bf49-04b84e07e77c", "14fb3e68-4dca-46db-bf49-04b84e07e77d"));

  @Autowired
  private ValidateSampleUnits validate;

  /**
   * Setup Mock responses when all created and injected into test subject.
   *
   * @throws Exception from FixtureHelper loading test data flat files.
   */
  @PostConstruct
  public void initIt() throws Exception {

    // Mock data layer domain objects CollectionExercise, SampleUnitGroup,
    // SampleUnit
    List<CollectionExercise> collectionExercises = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
    when(TestContext.collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.EXECUTED))
        .thenReturn(collectionExercises);

    List<ExerciseSampleUnitGroup> sampleUnitGroups = FixtureHelper.loadClassFixtures(ExerciseSampleUnitGroup[].class);
    when(TestContext.sampleUnitGroupRepo
        .findByStateFKAndCollectionExerciseInOrderByCreatedDateTimeAsc(SampleUnitGroupDTO.SampleUnitGroupState.INIT,
            collectionExercises, new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
                .thenReturn(sampleUnitGroups);
    when(TestContext.sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
        eq(SampleUnitGroupDTO.SampleUnitGroupState.INIT),
        any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(2L);
    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(0L);

    List<ExerciseSampleUnit> sampleUnits = FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);
    when(TestContext.sampleUnitRepo.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

    // Mock client Rest calls
    List<SurveyClassifierDTO> classifierTypeSelectors = FixtureHelper.loadClassFixtures(SurveyClassifierDTO[].class);
    when(TestContext.surveySvcClient.requestClassifierTypeSelectors(any())).thenReturn(classifierTypeSelectors);

    List<SurveyClassifierTypeDTO> classifierTypeSelector = FixtureHelper
        .loadClassFixtures(SurveyClassifierTypeDTO[].class);
    when(TestContext.surveySvcClient.requestClassifierTypeSelector(any(), any()))
        .thenReturn(classifierTypeSelector.get(0));

    List<PartyDTO> partyJson = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    when(TestContext.partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, "50000065975"))
        .thenReturn(partyJson.get(0));

    List<CollectionInstrumentDTO> collectionInstruments = FixtureHelper
        .loadClassFixtures(CollectionInstrumentDTO[].class);
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        "{\"RU_REF\":\"50000065975\",\"COLLECTION_EXERCISE\":\"14fb3e68-4dca-46db-bf49-04b84e07e77c\"}"))
            .thenReturn(collectionInstruments);
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        "{\"RU_REF\":\"50000065975\",\"COLLECTION_EXERCISE\":\"14fb3e68-4dca-46db-bf49-04b84e07e77d\"}"))
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
    ArgumentCaptor<ExerciseSampleUnit> sampleUnitSave = ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(TestContext.sampleUnitRepo, times(4)).saveAndFlush(sampleUnitSave.capture());
    List<ExerciseSampleUnit> savedSampleUnits = sampleUnitSave.getAllValues();
    assertTrue(savedSampleUnits.size() == 4);
    savedSampleUnits.forEach((sampleUnit) -> {
      assertTrue(sampleUnit.getSampleUnitPK() == 1);
      assertEquals("45297c23-763d-46a9-b4e5-c37ff5b4fbe8", sampleUnit.getPartyId().toString());
      assertEquals("5ca1afd6-4d01-4e13-bb73-acae62e2e540", sampleUnit.getCollectionInstrumentId().toString());
      assertEquals("50000065975", sampleUnit.getSampleUnitRef());
    });

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
        .forClass(ExerciseSampleUnitGroup.class);
    verify(TestContext.sampleUnitGroupRepo, times(4)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(resultCollectionId.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.VALIDATED, group.getStateFK());
      assertEquals("B", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(resultCollectionId.contains(exercise.getId().toString()));
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
    ArgumentCaptor<ExerciseSampleUnit> sampleUnitSave = ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(TestContext.sampleUnitRepo, times(0)).saveAndFlush(sampleUnitSave.capture());

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
        .forClass(ExerciseSampleUnitGroup.class);
    verify(TestContext.sampleUnitGroupRepo, times(0)).saveAndFlush(sampleUnitGroupSave.capture());

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(TestContext.collectRepo, times(0)).saveAndFlush(collectionExerciseSave.capture());
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
    ArgumentCaptor<ExerciseSampleUnit> sampleUnitSave = ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(TestContext.sampleUnitRepo, times(0)).saveAndFlush(sampleUnitSave.capture());

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
        .forClass(ExerciseSampleUnitGroup.class);
    verify(TestContext.sampleUnitGroupRepo, times(0)).saveAndFlush(sampleUnitGroupSave.capture());

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(TestContext.collectRepo, times(0)).saveAndFlush(collectionExerciseSave.capture());
  }

  /**
   * Test of party service client failure.
   */
  @Test
  public void validateSampleUnitsNoParty() {

    // Override happy path scenario to receive error from party service client
    when(TestContext.partySvcClient.requestParty(SampleUnitDTO.SampleUnitType.B, "50000065975"))
        .thenThrow(new RestClientException("Test failure of Party service"));
    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validate.validateSampleUnits();

    // Does not save sampleUnit details, leaves for correction and rerun user
    // story to be decided
    ArgumentCaptor<ExerciseSampleUnit> sampleUnitSave = ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(TestContext.sampleUnitRepo, times(0)).saveAndFlush(sampleUnitSave.capture());

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
        .forClass(ExerciseSampleUnitGroup.class);
    verify(TestContext.sampleUnitGroupRepo, times(4)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(resultCollectionId.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.FAILEDVALIDATION, group.getStateFK());
      assertEquals("B", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(resultCollectionId.contains(exercise.getId().toString()));
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
        "{\"RU_REF\":\"50000065975\",\"COLLECTION_EXERCISE\":\"14fb3e68-4dca-46db-bf49-04b84e07e77c\"}"))
            .thenThrow(new RestClientException("Test failure of Collection Instrument service"));
    when(TestContext.collectionInstrumentSvcClient.requestCollectionInstruments(
        "{\"RU_REF\":\"50000065975\",\"COLLECTION_EXERCISE\":\"14fb3e68-4dca-46db-bf49-04b84e07e77d\"}"))
            .thenThrow(new RestClientException("Test failure of Collection Instrument service"));

    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED), any()))
            .thenReturn(0L);
    when(TestContext.sampleUnitGroupRepo
        .countByStateFKAndCollectionExercise(eq(SampleUnitGroupDTO.SampleUnitGroupState.FAILEDVALIDATION), any()))
            .thenReturn(2L);

    validate.validateSampleUnits();

    // Does not save sampleUnit details, leaves for correction and rerun user
    // story to be decided
    ArgumentCaptor<ExerciseSampleUnit> sampleUnitSave = ArgumentCaptor.forClass(ExerciseSampleUnit.class);
    verify(TestContext.sampleUnitRepo, times(0)).saveAndFlush(sampleUnitSave.capture());

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave = ArgumentCaptor
        .forClass(ExerciseSampleUnitGroup.class);
    verify(TestContext.sampleUnitGroupRepo, times(4)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 4);
    savedSampleUnitGroups.forEach((group) -> {
      assertTrue(resultCollectionId.contains(group.getCollectionExercise().getId().toString()));
      assertEquals(SampleUnitGroupState.FAILEDVALIDATION, group.getStateFK());
      assertEquals("B", group.getFormType());
    });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave = ArgumentCaptor.forClass(CollectionExercise.class);
    verify(TestContext.collectRepo, times(2)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> exercises = collectionExerciseSave.getAllValues();
    assertTrue(exercises.size() == 2);
    exercises.forEach((exercise) -> {
      assertTrue(resultCollectionId.contains(exercise.getId().toString()));
      assertEquals(CollectionExerciseState.FAILEDVALIDATION, exercise.getState());
    });
  }
}
