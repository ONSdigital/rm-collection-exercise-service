package uk.gov.ons.ctp.response.collection.exercise.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.LockingException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.action.representation.ActionPlanDTO;
import uk.gov.ons.ctp.response.casesvc.message.sampleunitnotification.SampleUnitParent;
import uk.gov.ons.ctp.response.collection.exercise.client.ActionSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.PartySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
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
import uk.gov.ons.ctp.response.party.representation.PartyDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Tests for the SampleUnitDistributor */
@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

  private static final Integer DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX = 10;
  private static final String DISTRIBUTION_SCHEDULE_DELAY = "10";
  private static final int IMPOSSIBLE_ID = Integer.MAX_VALUE;

  private static final String COLLECTION_EXERCISE_ID = "14fb3e68-4dca-46db-bf49-04b84e07e77c";
  private static final String COLLECTION_INSTRUMENT_ID = "a9ed73c3-92b5-44d8-b350-4453729ebcf6";
  private static final String PARTY_ID_PARENT = "45297c23-763d-46a9-b4e5-c37ff5b4fbe8";
  private static final String SAMPLE_UNIT_REF = "50000065975";
  private static final String SAMPLE_UNIT_TYPE_PARENT = "B";
  private static final String ACTION_PLAN_ID_PARENT = "5381731e-e386-41a1-8462-26373744db86";
  private static final String TEST_EXCEPTION = "Test Exception thrown";

  @InjectMocks private SampleUnitDistributor sampleUnitDistributor;

  @Mock private SampleUnitPublisher publisher;

  @Mock private CollectionExerciseRepository collectionExerciseRepo;

  @Mock private EventRepository eventRepository;

  @Mock private SampleUnitRepository sampleUnitRepo;

  @Mock private SampleUnitGroupRepository sampleUnitGroupRepo;

  @Mock private ActionSvcClient actionSvcClient;

  @Mock private PartySvcClient partySvcClient;

  @Mock private SurveySvcClient surveySvcClient;

  @Mock
  private StateTransitionManager<
          CollectionExerciseDTO.CollectionExerciseState,
          CollectionExerciseDTO.CollectionExerciseEvent>
      collectionExerciseTransitionState;

  @Mock
  private StateTransitionManager<SampleUnitGroupState, SampleUnitGroupDTO.SampleUnitGroupEvent>
      sampleUnitGroupState;

  @Mock
  @Qualifier("distribution")
  private static DistributedListManager<Integer> sampleDistributionListManager;

  @Mock private PlatformTransactionManager platformTransactionManager;

  @Spy private AppConfig appConfig = new AppConfig();

  private CollectionExercise collectionExercise;
  private List<Event> events;
  private List<ExerciseSampleUnitGroup> sampleUnitGroups;
  private List<ExerciseSampleUnit> sampleUnitParentOnly;
  private List<ActionPlanDTO> actionPlans;
  private List<PartyDTO> parties;
  private List<SurveyDTO> surveys;

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
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));
    sampleUnitParentOnly =
        FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class, "ParentOnly");
    actionPlans = FixtureHelper.loadClassFixtures(ActionPlanDTO[].class);
    parties = FixtureHelper.loadClassFixtures(PartyDTO[].class);
    surveys = FixtureHelper.loadClassFixtures(SurveyDTO[].class);

    MockitoAnnotations.initMocks(this);

    // Mock calls to repositories and services
    when(sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupState.VALIDATED,
                collectionExercise,
                new ArrayList<>(Collections.singletonList(IMPOSSIBLE_ID)),
                new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
        .thenReturn(sampleUnitGroups);

    when(eventRepository.findOneByCollectionExerciseAndTag(
            collectionExercise, EventService.Tag.go_live.name()))
        .thenReturn(events.get(0));

    when(sampleUnitRepo.findBySampleUnitGroup(any())).thenReturn(sampleUnitParentOnly);

    when(partySvcClient.requestParty(any(), any())).thenReturn(parties.get(0));

    when(surveySvcClient.findSurvey(collectionExercise.getSurveyId())).thenReturn(surveys.get(0));

    when(actionSvcClient.getActionPlansBySelectors(any(), any(), any())).thenReturn(actionPlans);

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

  /** Test publishing of 2 sample unit groups each only containing one sample unit */
  @Test
  public void testSampleUnitPublished() {

    // Given setUp()

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then both sample units are published and
    // sample unit groups and collection exercise are transitioned
    ArgumentCaptor<SampleUnitParent> sampleUnitParentSave =
        ArgumentCaptor.forClass(SampleUnitParent.class);
    verify(publisher, times(2)).sendSampleUnit(sampleUnitParentSave.capture());
    List<SampleUnitParent> savedSampleUnitParents = sampleUnitParentSave.getAllValues();
    savedSampleUnitParents.forEach(
        sampleUnitParent -> {
          assertEquals(SAMPLE_UNIT_REF, sampleUnitParent.getSampleUnitRef());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, sampleUnitParent.getSampleUnitType());
          assertEquals(PARTY_ID_PARENT, sampleUnitParent.getPartyId());
          assertEquals(COLLECTION_INSTRUMENT_ID, sampleUnitParent.getCollectionInstrumentId());
          assertEquals(COLLECTION_EXERCISE_ID, sampleUnitParent.getCollectionExerciseId());
          assertEquals(ACTION_PLAN_ID_PARENT, sampleUnitParent.getActionPlanId());
          assertNull(sampleUnitParent.getSampleUnitChildren());
        });

    ArgumentCaptor<ExerciseSampleUnitGroup> sampleUnitGroupSave =
        ArgumentCaptor.forClass(ExerciseSampleUnitGroup.class);
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(sampleUnitGroupSave.capture());
    List<ExerciseSampleUnitGroup> savedSampleUnitGroups = sampleUnitGroupSave.getAllValues();
    savedSampleUnitGroups.forEach(
        group -> {
          assertEquals(COLLECTION_EXERCISE_ID, group.getCollectionExercise().getId().toString());
          assertEquals(SampleUnitGroupState.PUBLISHED, group.getStateFK());
          assertEquals(SAMPLE_UNIT_TYPE_PARENT, group.getFormType());
        });

    ArgumentCaptor<CollectionExercise> collectionExerciseSave =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectionExerciseRepo, times(1)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> savedCollectionExercise = collectionExerciseSave.getAllValues();
    savedCollectionExercise.forEach(
        exercise -> {
          assertEquals(COLLECTION_EXERCISE_ID, exercise.getId().toString());
          assertEquals(CollectionExerciseState.READY_FOR_LIVE, exercise.getState());
        });
  }

  /** Test collection exercise goes LIVE when go_live date has past at time of validation */
  @Test
  public void testChangeCollectionExerciseStateToLiveWhenGoLiveDatePast() {

    // Given collection exercise go live date is in the past
    events
        .get(0)
        .setTimestamp(new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10)));
    when(eventRepository.findOneByCollectionExerciseAndTag(
            collectionExercise, EventService.Tag.go_live.name()))
        .thenReturn(events.get(0));

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then collection exercise is transitioned to live
    ArgumentCaptor<CollectionExercise> collectionExerciseSave =
        ArgumentCaptor.forClass(CollectionExercise.class);
    verify(collectionExerciseRepo, times(1)).saveAndFlush(collectionExerciseSave.capture());
    List<CollectionExercise> savedCollectionExercise = collectionExerciseSave.getAllValues();
    savedCollectionExercise.forEach(
        exercise -> {
          assertEquals(COLLECTION_EXERCISE_ID, exercise.getId().toString());
          assertEquals(CollectionExerciseState.LIVE, exercise.getState());
        });
  }

  /** Test no sampleUnitGroups in state VALIDATED - none to distribute */
  @Test
  public void testNoSampleUnitGroupsExist() {

    // Given no sampleUnitGroups found for given exercise
    when(sampleUnitGroupRepo
            .findByStateFKAndCollectionExerciseAndSampleUnitGroupPKNotInOrderByModifiedDateTimeAsc(
                SampleUnitGroupState.VALIDATED,
                collectionExercise,
                new ArrayList<>(Collections.singletonList(IMPOSSIBLE_ID)),
                new PageRequest(0, DISTRIBUTION_SCHEDULE_RETRIEVAL_MAX)))
        .thenReturn(Collections.EMPTY_LIST);

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then we save no sample units and don't transition the collection exercise
    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test LockingException thrown by DistributedListManager */
  @Test
  public void testDistributedListManagerLockingException() throws Exception {

    // Given we fail to retrieve the sampleDistributionList
    when(sampleDistributionListManager.findList(any(String.class), any(boolean.class)))
        .thenThrow(LockingException.class);

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then we save no sample units and don't transition the collection exercise
    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test no sample units published when we fail to retrieve the business pastry */
  @Test
  public void testPartySvcRestClientException() {

    // Given we fail to retrieve the business party
    when(partySvcClient.requestParty(any(), any())).thenThrow(RestClientException.class);
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(0L);

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then we save no sample units and don't transition the collection exercise
    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test CTPException thrown by sampleUnitGroup state transition */
  @Test
  public void testSampleUnitGroupStateTransitionException() throws Exception {

    // Given we return a CTPException from SampleUnitGroup transition manager
    when(sampleUnitGroupState.transition(
            SampleUnitGroupState.VALIDATED, SampleUnitGroupEvent.PUBLISH))
        .thenThrow(new CTPException(CTPException.Fault.BAD_REQUEST, TEST_EXCEPTION));
    when(sampleUnitGroupRepo.countByStateFKAndCollectionExercise(
            eq(SampleUnitGroupDTO.SampleUnitGroupState.PUBLISHED), any()))
        .thenReturn(0L);

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then we save no sample units and don't transition the collection exercise
    verify(publisher, never()).sendSampleUnit(any());
    verify(sampleUnitGroupRepo, never()).saveAndFlush(any());
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }

  /** Test CTPException thrown by collectionExercise state transition */
  @Test
  public void testCollectionExerciseStateTransitionException() throws Exception {

    // Given we return a CTPException from collectionExercise transition manager
    when(collectionExerciseTransitionState.transition(
            CollectionExerciseState.VALIDATED, CollectionExerciseEvent.PUBLISH))
        .thenThrow(new CTPException(CTPException.Fault.BAD_REQUEST, TEST_EXCEPTION));

    // When
    sampleUnitDistributor.distributeSampleUnits(collectionExercise);

    // Then sampleunits are published but collection exercise is not transitioned
    verify(publisher, times(2)).sendSampleUnit(any(SampleUnitParent.class));
    verify(sampleUnitGroupRepo, times(2)).saveAndFlush(any(ExerciseSampleUnitGroup.class));
    verify(collectionExerciseRepo, never()).saveAndFlush(any());
  }
}
