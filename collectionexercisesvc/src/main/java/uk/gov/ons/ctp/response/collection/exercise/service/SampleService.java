package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

import java.util.UUID;

/**
 * Service responsible for dealing with samples
 *
 */
public interface SampleService {

  /**
   * Request the delivery of sample units from the Sample Service via a message
   * queue.
   *
   * @param id the Collection Exercise Id for which to request sample
   *          units.
   * @return the total number of sample units in the collection exercise.
   */
  SampleUnitsRequestDTO requestSampleUnits(final UUID id);

  /**
   * Save a SampleUnit
   *
   * @param sampleUnit the SampleUnit to save.
   * @return the SampleUnit saved.
   */
  ExerciseSampleUnit acceptSampleUnit(final SampleUnit sampleUnit);

}
