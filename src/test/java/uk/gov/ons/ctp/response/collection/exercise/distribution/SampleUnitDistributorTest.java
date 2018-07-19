package uk.gov.ons.ctp.response.collection.exercise.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.ScheduleSettings;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitType;

/** Tests for the SampleUnitDistributor */
@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;
  private static final String DISTRIBUTION_SCHEDULE_DELAY = "10";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_INSTRUMENT_ID = "a9ed73c3-92b5-44d8-b350-4453729ebcf6";
  private static final String PARTY_ID_PARENT = "908366c6-a158-4ea0-8c43-cb8199fc2f7f";
  private static final String PARTY_ID_CHILD = "e85f3aa9-1559-4406-956a-074d478cbcae";
  private static final String SAMPLE_UNIT_REF = "50000065975";
  private static final String SAMPLE_UNIT_TYPE_PARENT = "B";
  private static final String SAMPLE_UNIT_TYPE_CHILD = "BI";
  private static final String ACTION_PLAN_ID_PARENT = "e71002ac-3575-47eb-b87f-cd9db92bf9a7";
  private static final String ACTION_PLAN_ID_CHILD = "0009e978-0932-463b-a2a1-b45cb3ffcb2a";
  private static final String TEST_EXCEPTION = "Test Exception thrown";

  @InjectMocks private SampleUnitDistributor sampleUnitDistributor;

  @Mock private PlatformTransactionManager platformTransactionManager;

  @Mock private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Mock private CollectionExerciseRepository collectionExerciseRepo;

  @Mock private EventRepository eventRepository;

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupDTO.SampleUnitGroupEvent>
      sampleUnitGroupState;

  @Mock
  private StateTransitionManager<
          CollectionExerciseDTO.CollectionExerciseState,
          CollectionExerciseDTO.CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Mock private SampleUnitPublisher publisher;

  @Mock
  @Qualifier("distribution")
  private static DistributedListManager<Integer> sampleDistributionListManager;

  @Spy private AppConfig appConfig = new AppConfig();

  private List<ExerciseSampleUnitGroup> sampleUnitGroups;
  private CollectionExercise collectionExercise;
  private List<Event> events;
  private List<ExerciseSampleUnit> sampleUnitParentOnly;
  private List<ExerciseSampleUnit> sampleUnitRespondents;

  /**
   * Setup Mock responses.
   *
   * @throws Exception from FixtureHelper loading test data flat files.
   */
  @Before
  public void setUp() throws Exception {

    ScheduleSettings scheduleSettings = new ScheduleSettings();
    scheduleSettings.setDistributionScheduleDelayMilliSeconds(DISTRIBUTION_SCHEDULE_DELAY);
    scheduleSettings.setDistributionScheduleRetrievalMax(DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX);
    scheduleSettings.setValidationScheduleDelayMilliSeconds(DISTRIBUTION_SCHEDULE_DELAY);
    scheduleSettings.setValidationScheduleRetrievalMax(DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX);

    appConfig.setSchedules(scheduleSettings);

    sampleUnitGroups = FixtureHelper.loadClassFixtures(ExerciseSampleUnitGroup[].class);
    collectionExercise = sampleUnitGroups.get(0).getCollectionExercise();
    events = FixtureHelper.loadClassFixtures(Event[].class);
    sampleUnitParentOnly =
        FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class, "ParentOnly");
    sampleUnitRespondents =
        FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class, "WithRespondentUnits");

    MockitoAnnotations.initMocks(this);

    // Mock data layer domain objects SampleUnitGroup, SampleUnit and repository
    // queries.
    when(sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupState.VALIDATED,
                collectionExercise,
                new ArrayList<Integer>(Arrays.asList(IMPOSSIBLE_ID)),
                new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
        .thenReturn(sampleUnitGroups);

    when(sampleUnitRepo.findBySampleUnitGroup(any())).thenReturn(sampleUnitParentOnly);

    when(collectionExerciseRepo.getActiveActionPlanId(
            collectionExercise.getExercisePK(), "B", collectionExercise.getSurveyId()))
        .thenReturn(ACTION_PLAN_ID_PARENT);

    when(collectionExerciseRepo.getActiveActionPlanId(
            collectionExercise.getExercisePK(), "BI", collectionExercise.getSurveyId()))
        .thenReturn(ACTION_PLAN_ID_CHILD);
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(2L);

    // Mock transition Managers
    when(collectionExerciseTransitionState.transition(
            CollectionExerciseState.VALIDATED, CollectionExerciseEvent.PUBLISH))
        .thenReturn(CollectionExerciseState.READY_FOR_LIVE);

    when(collectionExerciseTransitionState.transition(
            CollectionExerciseState.VALIDATED, CollectionExerciseEvent.GO_LIVE))
        .thenReturn(CollectionExerciseState.LIVE);

    when(sampleUnitGroupState.transition(
            SampleUnitGroupState.VALIDATED, SampleUnitGroupEvent.PUBLISH))
        .thenReturn(SampleUnitGroupState.PUBLISHED);
  }

  /**
   * Test happy path of Party with no pre-enrolled respondent units. SampleUnitParent with no
   * SampleUnitChildren.
   */
  @Test
  public void sampleUnitParentPublishedWhenParentOnly() {

    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    ArgumentCaptor<SampleUnitParent> sampleUnitParentSave =
        ArgumentCaptor.forClass(SampleUnitParent.class);
    verify(publisher, times(2)).sendSampleUnit(sampleUnitParentSave.capture());
    List<SampleUnitParent> savedSampleUnitParents = sampleUnitParentSave.getAllValues();
    assertTrue(savedSampleUnitParents.size() == 2);
    savedSampleUnitParents.forEach(
        (message) -> {
          assertEquals(SAMPLE_UNIT_REF, message.getSampleUnitRef());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, message.getSampleUnitType());
          assertEquals(PARTY_ID_PARENT, message.getPartyId());
          assertEquals(COLLECTION_INSTRUMENT_ID, message.getCollectionInstrumentId());
          assertEquals(COLLECTION_EXERCISE_ID, message.getCollectionExerciseId());
          assertEquals(ACTION_PLAN_ID_PARENT, message.getActionPlanId());
          assertNull(message.getSampleUnitChildren());
        });

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave =
        ArgumentCaptor.forClass(ExerciseSampleUnitGroup.class);
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 2);
    savedSampleUnitGroups.forEach(
        (group) -> {
          assertEquals(COLLECTION_EXERCISE_ID, group.getCollectionExercise().getId().toString());
          assertEquals(SampleUnitGroupState.PUBLISHED, group.getStateFK());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, group.getFormType());
        });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectionExerciseRepo, times(1)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> savedCollectionExercise = collectionExerciseSave.getAllValues();
    assertTrue(savedCollectionExercise.size() == 1);
    savedCollectionExercise.forEach(
        (exercise) -> {
          assertEquals(COLLECTION_EXERCISE_ID, exercise.getId().toString());
          assertEquals(CollectionExerciseState.READY_FOR_LIVE, exercise.getState());
        });
  }

  /**
   * Test happy path of Party with pre-enrolled respondent units. SampleUnitParent with
   * SampleUnitChildren.
   */
  @Test
  public void sampleUnitParentPublishedWhenChildIsNotNull() {

    // Override to return respondent units
    when(sampleUnitRepo.findBySampleUnitGroup(any())).thenReturn(sampleUnitRespondents);

    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    ArgumentCaptor<SampleUnitParent> sampleUnitParentSave =
        ArgumentCaptor.forClass(SampleUnitParent.class);
    verify(publisher, times(2)).sendSampleUnit(sampleUnitParentSave.capture());
    List<SampleUnitParent> savedSampleUnitParents = sampleUnitParentSave.getAllValues();
    assertTrue(savedSampleUnitParents.size() == 2);
    savedSampleUnitParents.forEach(
        (message) -> {
          assertEquals(SAMPLE_UNIT_REF, message.getSampleUnitRef());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, message.getSampleUnitType());
          assertEquals(PARTY_ID_PARENT, message.getPartyId());
          assertEquals(COLLECTION_INSTRUMENT_ID, message.getCollectionInstrumentId());
          assertEquals(COLLECTION_EXERCISE_ID, message.getCollectionExerciseId());
          assertEquals(ACTION_PLAN_ID_PARENT, message.getActionPlanId());
          assertEquals(
              SAMPLE_UNIT_REF,
              message.getSampleUnitChildren().getSampleUnitchildren().get(0).getSampleUnitRef());
          assertEquals(
              SAMPLE_UNIT_TYPE_CHILD,
              message.getSampleUnitChildren().getSampleUnitchildren().get(0).getSampleUnitType());
          assertEquals(
              PARTY_ID_CHILD,
              message.getSampleUnitChildren().getSampleUnitchildren().get(0).getPartyId());
          assertEquals(
              COLLECTION_INSTRUMENT_ID,
              message
                  .getSampleUnitChildren()
                  .getSampleUnitchildren()
                  .get(0)
                  .getCollectionInstrumentId());
          assertEquals(
              ACTION_PLAN_ID_CHILD,
              message.getSampleUnitChildren().getSampleUnitchildren().get(0).getActionPlanId());
        });

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave =
        ArgumentCaptor.forClass(ExerciseSampleUnitGroup.class);
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 2);
    savedSampleUnitGroups.forEach(
        (group) -> {
          assertEquals(COLLECTION_EXERCISE_ID, group.getCollectionExercise().getId().toString());
          assertEquals(SampleUnitGroupState.PUBLISHED, group.getStateFK());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, group.getFormType());
        });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectionExerciseRepo, times(1)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> savedCollectionExercise = collectionExerciseSave.getAllValues();
    assertTrue(savedCollectionExercise.size() == 1);
    savedCollectionExercise.forEach(
        (exercise) -> {
          assertEquals(COLLECTION_EXERCISE_ID, exercise.getId().toString());
          assertEquals(CollectionExerciseState.READY_FOR_LIVE, exercise.getState());
        });
  }

  /** Test of no sampleUnitGroups in state VALIDATED - none to distribute. */
  @Test
  public void noSampleUnitGroupsExistTest() {

    // Override happy path scenario to return an empty list querying for
    // sampleUnitGroups.
    when(sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupState.VALIDATED,
                collectionExercise,
                new ArrayList<Integer>(Arrays.asList(IMPOSSIBLE_ID)),
                new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
        .thenReturn(new ArrayList<ExerciseSampleUnitGroup>());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test no SampleUnitChild or ActionPlanId in SampleUnitGroup. */
  @Test
  public void noSampleUnitChildOrActionPlanIdStillCreateBCase() {

    // Override happy path scenario so no ActionPlanId is returned.
    when(collectionExerciseRepo.getActiveActionPlanId(
            collectionExercise.getExercisePK(), "B", collectionExercise.getSurveyId()))
        .thenReturn(null);

    // Count of SampleUnitGroups would not match as didn't publish the
    // SampleUnitGroups in the exercise as no child or ActionPlanId.
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(0L);
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    ArgumentCaptor<SampleUnitParent> sampleUnitParentSave =
        ArgumentCaptor.forClass(SampleUnitParent.class);
    verify(publisher, times(2)).sendSampleUnit(sampleUnitParentSave.capture());
    List<SampleUnitParent> savedSampleUnitParents = sampleUnitParentSave.getAllValues();
    assertTrue(savedSampleUnitParents.size() == 2);
    savedSampleUnitParents.forEach(
        (message) -> {
          assertEquals(SAMPLE_UNIT_REF, message.getSampleUnitRef());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, message.getSampleUnitType());
          assertEquals(PARTY_ID_PARENT, message.getPartyId());
          assertEquals(COLLECTION_INSTRUMENT_ID, message.getCollectionInstrumentId());
          assertEquals(COLLECTION_EXERCISE_ID, message.getCollectionExerciseId());
          assertEquals(null, message.getActionPlanId());
        });
    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave =
        ArgumentCaptor.forClass(ExerciseSampleUnitGroup.class);
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    assertTrue(savedSampleUnitGroups.size() == 2);
    savedSampleUnitGroups.forEach(
        (group) -> {
          assertEquals(COLLECTION_EXERCISE_ID, group.getCollectionExercise().getId().toString());
          assertEquals(SampleUnitGroupState.PUBLISHED, group.getStateFK());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, group.getFormType());
        });
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test no SampleUnitParent in SampleUnitGroup */
  @Test
  public void noParentInSampleUnitGroup() {

    // Override happy path scenario, set SampleUnitGroup to contain child only
    sampleUnitParentOnly.get(0).setSampleUnitType(SampleUnitType.BI);

    // Count of SampleUnitGroups would not match as didn't publish the
    // SampleUnitGroups in the exercise as had no Parent.
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(0L);
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test of LockingException thrown by DistributedListManager. */
  @Test
  public void distributedListManagerLockingException() {
    // Override happy path scenario to return a LockingException from
    // DistributedListManager.
    try {
      when(sampleDistributionListManager.findList(any(String.class), any(boolean.class)))
          .thenThrow(new LockingException("TEST_EXCEPTION"));
    } catch (LockingException ex) {
      // Do nothing with it, actually want to catch it in SampleUnitDistributor
    }
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test of Exception thrown by sampleUnitGroup state transition. */
  @Test
  public void sampleUnitGroupStateTransitionException() {
    // Override happy path scenario to return a CTPException from
    // SampleUnitGroup transition manager.
    try {
      when(sampleUnitGroupState.transition(
              SampleUnitGroupState.VALIDATED, SampleUnitGroupEvent.PUBLISH))
          .thenThrow(new CTPException(CTPException.Fault.BAD_REQUEST, TEST_EXCEPTION));
    } catch (CTPException ex) {
      // Do nothing with it, actually want to catch it in SampleUnitDistributor
    }

    // Count of SampleUnitGroups would not match as didn't publish the
    // SampleUnitGroup due to state transition failure
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(0L);
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test of Exception thrown by collectionExercise state transition. */
  @Test
  public void collectionExerciseStateTransitionException() throws Exception {
    // Override happy path scenario to return a CTPException from
    // collectionExercise transition manager.
    try {
      when(collectionExerciseTransitionState.transition(
              CollectionExerciseState.VALIDATED, CollectionExerciseEvent.PUBLISH))
          .thenThrow(new CTPException(CTPException.Fault.BAD_REQUEST, TEST_EXCEPTION));
    } catch (CTPException ex) {
      // Do nothing with it, actually want to catch it in SampleUnitDistributor
    }
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    verify(publisher, times(2)).sendSampleUnit(any(SampleUnitParent.class));
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(any(ExerciseSampleUnitGroup.class));
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test if go_live date has past at time of validation. */
  @Test
  public void changeCollectionExerciseStateToLiveWhenGoLiveDatePast() throws Exception {
    // Set collection exercise go live date to be in past
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)));
    doReturn(events.get(0))
        .when(eventRepository)
        .findOneByCollectionExerciseAndTag(collectionExercise, EventService.Tag.go_live.name());

    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    ArgumentCaptor<CollectionExercise> collectionExerciseSave =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectionExerciseRepo, times(1)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> savedCollectionExercise = collectionExerciseSave.getAllValues();
    assertTrue(savedCollectionExercise.size() == 1);
    savedCollectionExercise.forEach(
        (exercise) -> {
          assertEquals(COLLECTION_EXERCISE_ID, exercise.getId().toString());
          assertEquals(CollectionExerciseState.LIVE, exercise.getState());
        });
  }
}
