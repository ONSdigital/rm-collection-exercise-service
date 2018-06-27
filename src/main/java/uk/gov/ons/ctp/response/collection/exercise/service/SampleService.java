package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import java.util.UUID;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.ExerciseSampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;
import uk.gov.ons.ctp.response.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.sampleunit.definition.SampleUnit;

/** Service responsible for dealing with samples */
public interface SampleService {

  /**
   * Request the delivery of sample units from the Sample Service via a message queue.
   *
   * @param id the Collection Exercise Id for which to request sample units.
   * @return the total number of sample units in the collection exercise.
   * @throws CTPException when collection exercise state transition error
   */
  SampleUnitsRequestDTO requestSampleUnits(UUID id) throws CTPException;

  /**
   * Save a SampleUnit
   *
   * @param sampleUnit the SampleUnit to save.
   * @return the SampleUnit saved.
   * @throws CTPException when collection exercise state transition error
   */
  ExerciseSampleUnit acceptSampleUnit(SampleUnit sampleUnit) throws CTPException;

  /** Validate SampleUnits */
  void validateSampleUnits();

  /**
   * Distribute Sample Units for a CollectionExercise
   *
   * @param exercise for which to distribute SampleUnits.
   */
  void distributeSampleUnits(CollectionExercise exercise);

  /**
   * Get the sample unit validation errors for a given collection exercise
   *
   * @param collectionExerciseId a collection exercise
   * @return an array of validation errors
   */
  SampleUnitValidationErrorDTO[] getValidationErrors(UUID collectionExerciseId);

  /**
   * Gets a list of sample links for a given sample summary
   *
   * @param sampleSummaryId the id of the sample summary to find sample links for
   * @return a list of sample links
   */
  List<SampleLink> getSampleLinksForSummary(UUID sampleSummaryId);

  /**
   * Method to save a sample link
   *
   * @param sampleLink the sample link to save
   * @return the updated sample link
   */
  SampleLink saveSampleLink(SampleLink sampleLink);
}
