package uk.gov.ons.ctp.response.collection.exercise.distribution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ons.ctp.common.FixtureHelper;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.config.ScheduleSettings;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleUnitPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitGroupRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleUnitRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO;

import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SampleUnitDistributorTest {

    @InjectMocks
    private SampleUnitDistributor sampleUnitDistributor;

    @Mock
    private SampleUnitGroupRepository sampleUnitGroupRepository;

    @Mock
    private CollectionExerciseRepository collectionExerciseRepository;

    @Mock
    private SampleUnitRepository sampleUnitRepository;

    @Mock
    private StateTransitionManager<SampleUnitGroupState, uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent> sampleUnitGroupState;

    @Mock
    private SampleUnitPublisher publisher;

    @Spy
    private AppConfig appConfig = new AppConfig();


    private final long ZERO_SAMPLE_GROUPS_EXIST = 0L;
    private final long SAMPLE_GROUPS_EXIST = 1L;

    private List<CollectionExercise> collectionExercises;
    private List<ExerciseSampleUnit> sampleUnitList;
    private ExerciseSampleUnitGroup exerciseSampleUnitGroup = new ExerciseSampleUnitGroup();
    private ExerciseSampleUnitGroup exerciseSampleUnitGroupNoSurvey = new ExerciseSampleUnitGroup();
    private ExerciseSampleUnit sampleUnitParent;
    private ExerciseSampleUnit sampleUnitChild;

    @Before
    public void setup() throws Exception {

        ScheduleSettings scheduleSettings = new ScheduleSettings();
        scheduleSettings.setDistributionScheduleDelayMilliSeconds("10");
        scheduleSettings.setDistributionScheduleRetrievalMax(10);
        scheduleSettings.setValidationScheduleDelayMilliSeconds("10");
        scheduleSettings.setValidationScheduleRetrievalMax(10);

        appConfig.setSchedules(scheduleSettings);

        collectionExercises = FixtureHelper.loadClassFixtures(CollectionExercise[].class);
        sampleUnitList = FixtureHelper.loadClassFixtures(ExerciseSampleUnit[].class);



        exerciseSampleUnitGroup.setCollectionExercise(collectionExercises.get(0));
        exerciseSampleUnitGroup.setStateFK(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED);
        exerciseSampleUnitGroup.setSampleUnitGroupPK(1);

        exerciseSampleUnitGroupNoSurvey.setCollectionExercise(collectionExercises.get(1));
        exerciseSampleUnitGroupNoSurvey.setStateFK(SampleUnitGroupDTO.SampleUnitGroupState.VALIDATED);

        sampleUnitChild = sampleUnitList.get(0);
        sampleUnitChild.setSampleUnitGroup(exerciseSampleUnitGroup);

        sampleUnitParent = sampleUnitList.get(1);
        sampleUnitParent.setSampleUnitGroup(exerciseSampleUnitGroup);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void noSampleUnitGroupsExistTest() throws CTPException {
        CollectionExercise collectionExercise = collectionExercises.get(0);

        when(sampleUnitGroupRepository.countByStateFKAndCollectionExercise(any(),any())).thenReturn(ZERO_SAMPLE_GROUPS_EXIST);
        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(collectionExerciseRepository, times(1)).saveAndFlush(any());
    }

    @Test
    public void someSampleUnitGroupsExistTest() throws CTPException {
        CollectionExercise collectionExercise = collectionExercises.get(0);

        when(sampleUnitGroupRepository.countByStateFKAndCollectionExercise(any(),any())).thenReturn(SAMPLE_GROUPS_EXIST);
        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(collectionExerciseRepository, times(0)).saveAndFlush(any());
    }

    @Test
    public void correctNumberOfSampleUnitsDistributed() throws CTPException {
        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(new ExerciseSampleUnitGroup());
        sampleUnitGroups.add(new ExerciseSampleUnitGroup());
        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        sampleUnitDistributor.distributeSampleUnits(new CollectionExercise());
        verify(sampleUnitRepository
                , times(2)).findBySampleUnitGroup(any());
    }

    @Test
    public void sampleUnitParentCreatedWhenOfParentType() throws CTPException {
        CollectionExercise collectionExercise = exerciseSampleUnitGroup.getCollectionExercise();

        ExerciseSampleUnit sampleUnit = new ExerciseSampleUnit();
        sampleUnit.setSampleUnitGroup(exerciseSampleUnitGroup);

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroup);

        List<ExerciseSampleUnit> sampleUnits = new ArrayList<>();
        sampleUnits.add(sampleUnitParent);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        when(sampleUnitRepository.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(collectionExerciseRepository, times(1)).getActiveActionPlanId(any(), any(),
                any());
    }

    @Test
    public void sampleUnitChildCreatedWhenOfChildType() throws CTPException { //Doesn't really check if it's specifically a child
        CollectionExercise collectionExercise = exerciseSampleUnitGroup.getCollectionExercise();

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroup);

        List<ExerciseSampleUnit> sampleUnits = new ArrayList<>();
        sampleUnits.add(sampleUnitChild);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        when(sampleUnitRepository.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(collectionExerciseRepository, times(1)).getActiveActionPlanId(any(), any(),
                any());

    }

   /* @Test
    public void sampleUnitPublishedWhenChildIsNull() throws CTPException {
        CollectionExercise collectionExercise = exerciseSampleUnitGroup.getCollectionExercise();

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroup);

        List<ExerciseSampleUnit> sampleUnits = new ArrayList<>();
        sampleUnits.add(sampleUnitParent);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        when(sampleUnitRepository.findBySampleUnitGroup(any())).thenReturn(sampleUnits);
        when(collectionExerciseRepository.getActiveActionPlanId(any(),any(),any())).thenReturn("A");

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(publisher, times(1)).sendSampleUnit(any());
    }

    @Test
    public void sampleUnitPublishedWhenChildIsNotNull() throws CTPException {
        CollectionExercise collectionExercise = exerciseSampleUnitGroup.getCollectionExercise();

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroup);

        List<ExerciseSampleUnit> sampleUnits = new ArrayList<>();
        sampleUnits.add(sampleUnitParent);
        sampleUnits.add(sampleUnitChild);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        when(sampleUnitRepository.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

        when(sampleUnitGroupState.transition(any(), any())).thenReturn(SampleUnitGroupState.PUBLISHED);

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(sampleUnitGroupState, times(1)).transition(any(),any());
        verify(publisher, times(1)).sendSampleUnit(any());
    }*/

    @Test
    public void NothingPublishedWhenParentIsNull() throws CTPException {
        CollectionExercise collectionExercise = exerciseSampleUnitGroupNoSurvey.getCollectionExercise();

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroup);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(publisher, times(0)).sendSampleUnit(any());
    }

    @Test
    public void NothingPublishedWhenParentActionPlanIdIsNull() throws CTPException {
        CollectionExercise collectionExercise = exerciseSampleUnitGroup.getCollectionExercise();

        List<ExerciseSampleUnitGroup> sampleUnitGroups = new ArrayList<>();
        sampleUnitGroups.add(exerciseSampleUnitGroupNoSurvey);

        List<ExerciseSampleUnit> sampleUnits = new ArrayList<>();
        sampleUnits.add(sampleUnitParent);

        when(sampleUnitGroupRepository.findByStateFKAndCollectionExerciseOrderByModifiedDateTimeAsc(any(),any(),any()))
                .thenReturn(sampleUnitGroups);
        when(sampleUnitRepository.findBySampleUnitGroup(any())).thenReturn(sampleUnits);

        sampleUnitDistributor.distributeSampleUnits(collectionExercise);

        verify(publisher, times(0)).sendSampleUnit(any());
    }
}
