package uk.gov.ons.ctp.response.collection.exercise.validation;

import java.util.function.Function;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;

/**
 * Classifier Types used to search the Collection Instrument Service to return
 * the appropriate collection instrument for a sample unit. The classifiers used
 * in the Collection Instrument Service Search.
 *
 * Note. These classifier types need to match the classifier types returned from
 * the Survey service for the Collection Instrument Selector Type, with an
 * appropriate lambda expression to obtain the value.
 */
public enum CollectionInstrumentClassifierTypes implements Function<ExerciseSampleUnit, String> {
  COLLECTION_EXERCISE(unit -> unit.getSampleUnitGroup().getCollectionExercise().getId().toString()),
  RU_REF(unit -> unit.getSampleUnitRef());

  private final Function<ExerciseSampleUnit, String> func;

  /**
   * Create an instance of the enum.
   * @param lambda expression to be applied to obtain value for classifier.
   */
  CollectionInstrumentClassifierTypes(Function<ExerciseSampleUnit, String> lambda) {
    this.func = lambda;
  }

  @Override
  public String apply(ExerciseSampleUnit unit) {
    return func.apply(unit);
  }

}
