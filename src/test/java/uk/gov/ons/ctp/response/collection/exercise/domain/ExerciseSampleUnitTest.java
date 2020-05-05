package uk.gov.ons.ctp.response.collection.exercise.domain;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO.SampleUnitType;

public class ExerciseSampleUnitTest {

  private static final String ACTIVE_ACTION_PLAN_ID = "897b7564-cc31-49d6-afb5-2652c62b5ab4";
  private static final UUID COLLECTION_EXERCISE_ID =
      UUID.fromString("6ec01dfa-aff7-41aa-9b36-a8eb8a09f370");
  private static final UUID SAMPLE_UNIT_ID =
      UUID.fromString("949182f8-1838-45c6-9488-4e0689d84bce");
  private static final String SAMPLE_UNIT_REF = "50000065975";
  private static final UUID PARTY_ID = UUID.fromString("b0eebb64-513a-4748-9c0a-cecaab7acab7");
  private static final UUID COLLECTION_INSTRUMENT_ID =
      UUID.fromString("32abf434-aa37-4dd5-a611-4985f1a2e5b6");

  @Test
  public void toSampleUnitChild_ActionPlanId() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getActionPlanId(),
        is(ACTIVE_ACTION_PLAN_ID));
  }

  @Test
  public void toSampleUnitChild_Id() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitId(SAMPLE_UNIT_ID);

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getId(),
        is(SAMPLE_UNIT_ID.toString()));
  }

  @Test
  public void toSampleUnitChild_SampleUnitRef() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitRef(SAMPLE_UNIT_REF);

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getSampleUnitRef(),
        is(SAMPLE_UNIT_REF));
  }

  @Test
  public void toSampleUnitChild_SampleUnitType() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getSampleUnitType(),
        is(SampleUnitType.B.toString()));
  }

  @Test
  public void toSampleUnitChild_PartyId() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setPartyId(PARTY_ID);

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getPartyId(),
        is(PARTY_ID.toString()));
  }

  @Test
  public void toSampleUnitChild_CollectionInstrument() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

    assertThat(
        exerciseSampleUnit.toSampleUnitChild(ACTIVE_ACTION_PLAN_ID).getCollectionInstrumentId(),
        is(COLLECTION_INSTRUMENT_ID.toString()));
  }

  @Test
  public void toSampleUnitParent_ActionPlanId() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getActionPlanId(),
        is(ACTIVE_ACTION_PLAN_ID));
  }

  @Test
  public void toSampleUnitParent_Id() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitId(SAMPLE_UNIT_ID);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getId(),
        is(SAMPLE_UNIT_ID.toString()));
  }

  @Test
  public void toSampleUnitParent_SampleUnitRef() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitRef(SAMPLE_UNIT_REF);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getSampleUnitRef(),
        is(SAMPLE_UNIT_REF));
  }

  @Test
  public void toSampleUnitParent_SampleUnitType() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getSampleUnitType(),
        is(SampleUnitType.B.toString()));
  }

  @Test
  public void toSampleUnitParent_PartyId() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setPartyId(PARTY_ID);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getPartyId(),
        is(PARTY_ID.toString()));
  }

  @Test
  public void toSampleUnitParent_CollectionInstrument() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getCollectionInstrumentId(),
        is(COLLECTION_INSTRUMENT_ID.toString()));
  }

  @Test
  public void toSampleUnitParent_CollectionExercise() {
    final ExerciseSampleUnit exerciseSampleUnit = getExerciseSampleUnit();
    exerciseSampleUnit.setCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);

    assertThat(
        exerciseSampleUnit
            .toSampleUnitParent(ACTIVE_ACTION_PLAN_ID, COLLECTION_EXERCISE_ID)
            .getCollectionExerciseId(),
        is(COLLECTION_EXERCISE_ID.toString()));
  }

  private ExerciseSampleUnit getExerciseSampleUnit() {
    final ExerciseSampleUnit exerciseSampleUnit = new ExerciseSampleUnit();
    exerciseSampleUnit.setSampleUnitId(UUID.randomUUID());
    exerciseSampleUnit.setSampleUnitType(SampleUnitType.B);
    exerciseSampleUnit.setCollectionInstrumentId(UUID.randomUUID());
    return exerciseSampleUnit;
  }
}
