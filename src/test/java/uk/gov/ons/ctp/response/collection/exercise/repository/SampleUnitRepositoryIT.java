package uk.gov.ons.ctp.response.collection.exercise.repository;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitDTO.SampleUnitType;

@Slf4j
@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SampleUnitRepositoryIT {

  // Gubbins to make spring wire itself up
  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  // Actual stuff that we want injected
  @Autowired private CollectionExerciseRepository collexRepo;
  @Autowired private SampleUnitRepository sampleUnitRepo;
  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  @After
  public void tearDown() {
    // This really shouldn't be needed but some of the other tests seem to expect an empty DB
    sampleUnitRepo.deleteAll();
    sampleUnitGroupRepo.deleteAll();
    collexRepo.deleteAll();
  }

  @Test
  public void testSampleUnitNonExistent() {
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setState(CollectionExerciseState.CREATED);
    collectionExercise.setExerciseRef("ZZZ666");
    collectionExercise = collexRepo.saveAndFlush(collectionExercise);

    boolean actual =
        sampleUnitRepo.existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
            "ABC123", SampleUnitType.B, collectionExercise);

    assertEquals(false, actual);
  }

  @Test
  public void testSampleUnitExists() {
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setState(CollectionExerciseState.CREATED);
    collectionExercise.setExerciseRef("ZZZ666");
    collectionExercise = collexRepo.saveAndFlush(collectionExercise);

    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    sampleUnitGroup.setFormType("B");
    sampleUnitGroup.setStateFK(SampleUnitGroupState.INIT);
    sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

    ExerciseSampleUnit exerciseSampleUnit = new ExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
    exerciseSampleUnit.setSampleUnitRef("ABC123");
    exerciseSampleUnit.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);
    sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

    boolean actual =
        sampleUnitRepo.existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
            "ABC123", SampleUnitType.B, collectionExercise);

    assertEquals(true, actual);
  }

  @Test
  public void testFindByCollexAndState() {
    CollectionExercise collectionExercise = new CollectionExercise();
    collectionExercise.setId(UUID.randomUUID());
    collectionExercise.setState(CollectionExerciseState.CREATED);
    collectionExercise.setExerciseRef("88BB00AA");
    collectionExercise = collexRepo.saveAndFlush(collectionExercise);

    ExerciseSampleUnitGroup sampleUnitGroup = new ExerciseSampleUnitGroup();
    sampleUnitGroup.setCollectionExercise(collectionExercise);
    sampleUnitGroup.setFormType("B");
    sampleUnitGroup.setStateFK(SampleUnitGroupState.FAILEDVALIDATION);
    sampleUnitGroup.setCreatedDateTime(new Timestamp(new Date().getTime()));
    sampleUnitGroup = sampleUnitGroupRepo.saveAndFlush(sampleUnitGroup);

    ExerciseSampleUnit exerciseSampleUnit = new ExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
    exerciseSampleUnit.setSampleUnitRef("D1SC0");
    exerciseSampleUnit.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);
    sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

    exerciseSampleUnit = new ExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitGroup(sampleUnitGroup);
    exerciseSampleUnit.setSampleUnitRef("B15C17");
    exerciseSampleUnit.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);
    sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

    List<ExerciseSampleUnit> sampleUnits =
        sampleUnitRepo.findBySampleUnitGroupCollectionExerciseAndSampleUnitGroupStateFK(
            collectionExercise, SampleUnitGroupState.FAILEDVALIDATION);

    assertEquals(2, sampleUnits.size());
  }
}
