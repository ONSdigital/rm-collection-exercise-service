package uk.gov.ons.ctp.response.collection.exercise.client;

import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;

/**
 * Service responsible for making client calls to the Sample service
 *
 */
public interface SampleSvcClient {

  /**
   * Request the delivery of sample units from the Sample Service via a message
   * queue.
   *
   * @param exercise the Collection Exercise for which to request sample
   *          units.
   * @return the total number of sample units in the collection exercise.
   */
  SampleUnitsRequestDTO requestSampleUnits(CollectionExercise exercise);

}
