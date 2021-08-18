package uk.gov.ons.ctp.response.collection.exercise.repository;

import static org.junit.Assert.*;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnitGroup;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO.SampleUnitType;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;

/** Integration tests */
@ContextConfiguration
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:/application-test.yml")
public class SampleUnitRepositoryIT {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitRepositoryIT.class);

  // Gubbins to make spring wire itself up
  @ClassRule public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule public final SpringMethodRule springMethodRule = new SpringMethodRule();

  // Actual stuff that we want injected
  @Autowired private CollectionExerciseRepository collexRepo;
  @Autowired private SampleUnitRepository sampleUnitRepo;
  @Autowired private SampleUnitGroupRepository sampleUnitGroupRepo;

  /** Integration test */
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

    assertFalse(actual);

    // Tear down specific entities because of dodgy data created by other tests
    collexRepo.delete(collectionExercise);
  }

  /** Integration test */
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
    exerciseSampleUnit = sampleUnitRepo.saveAndFlush(exerciseSampleUnit);

    boolean actual =
        sampleUnitRepo.existsBySampleUnitRefAndSampleUnitTypeAndSampleUnitGroupCollectionExercise(
            "ABC123", SampleUnitType.B, collectionExercise);

    assertTrue(actual);

    // Tear down specific entities because of dodgy data created by other tests
    sampleUnitRepo.delete(exerciseSampleUnit);
    sampleUnitGroupRepo.delete(sampleUnitGroup);
    collexRepo.delete(collectionExercise);
  }

  /** Integration test */
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

    ExerciseSampleUnit exerciseSampleUnitOne = new ExerciseSampleUnit();
    exerciseSampleUnitOne.setSampleUnitGroup(sampleUnitGroup);
    exerciseSampleUnitOne.setSampleUnitRef("D1SC0");
    exerciseSampleUnitOne.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnitOne.setSampleUnitType(SampleUnitType.B);
    exerciseSampleUnitOne = sampleUnitRepo.saveAndFlush(exerciseSampleUnitOne);

    ExerciseSampleUnit exerciseSampleUnitTwo = new ExerciseSampleUnit();
    exerciseSampleUnitTwo.setSampleUnitGroup(sampleUnitGroup);
    exerciseSampleUnitTwo.setSampleUnitRef("B15C17");
    exerciseSampleUnitTwo.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnitTwo.setSampleUnitType(SampleUnitType.B);
    exerciseSampleUnitTwo = sampleUnitRepo.saveAndFlush(exerciseSampleUnitTwo);

    List<ExerciseSampleUnit> sampleUnits =
        sampleUnitRepo.findBySampleUnitGroupCollectionExerciseAndSampleUnitGroupStateFK(
            collectionExercise, SampleUnitGroupState.FAILEDVALIDATION);

    assertEquals(2, sampleUnits.size());

    // Tear down specific entities because of dodgy data created by other tests
    sampleUnitRepo.delete(exerciseSampleUnitOne);
    sampleUnitRepo.delete(exerciseSampleUnitTwo);
    sampleUnitGroupRepo.delete(sampleUnitGroup);
    collexRepo.delete(collectionExercise);
  }
}
