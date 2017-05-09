package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/**
 * Service responsible for dealing with samples
 *
 */
public interface SampleService {

  /**
   * Request the delivery of sample units from the Sample Service via a message
   * queue.
   *
   * @param exerciseId the Collection Exercise Id for which to request sample
   *          units.
   * @return the total number of sample units in the collection exercise.
   */
  Integer requestSampleUnits(final Integer exerciseId);

  /**
   * Save a SampleUnit
   *
   * @param sampleUnit the SampleUnit to save.
   * @return the SampleUnit saved.
   */
  ExerciseSampleUnit acceptSampleUnit(final SampleUnit sampleUnit);

}
