package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/**
 * Service responsible for dealing with collection exercises
 *
 */
public interface CollectionExerciseService {

  /**
   * Find a list of surveys associated to a collection exercise Id from the
   * Collection Exercise Service
   *
   * @param survey the survey for which to find collection exercises
   * @return the associated surveys.
   */
  List<CollectionExercise> findCollectionExercisesForSurvey(SurveyDTO survey);

  /**
   * Find a collection exercise associated to a collection exercise Id from the
   * Collection Exercise Service
   *
   * @param id the collection exercise Id for which to find collection exercise
   * @return the associated collection exercise.
   */
  CollectionExercise findCollectionExercise(UUID id);

  /**
   * Find all Collection Exercises
   *
   * @return a list of all Collection Exercises
   */
  List<CollectionExercise> findAllCollectionExercise();

  /**
   * Find a collection exercise from a survey ref (e.g. 221) and a collection exercise ref (e.g. 201808)
   * @param surveyRef the survey ref
   * @param exerciseRef the collection exercise ref
   * @return the specified collection exercise or null if not found
   */
  CollectionExercise findCollectionExercise(String surveyRef, String exerciseRef);

  /**
   * find a list of all sample summary linked to a collection exercise
   *
   * @param id the collection exercise Id to find the linked sample summaries
   *          for
   * @return list of linked sample summary
   */
  List<SampleLink> findLinkedSampleSummaries(UUID id);

  /**
   * Find case types associated to a collection exercise from the Collection
   * Exercise Service
   *
   * @param collectionExercise the collection exercise for which to find case
   *          types
   * @return the associated case type DTOs.
   */
  Collection<CaseType> getCaseTypesList(CollectionExercise collectionExercise);

  /**
   * Delete existing SampleSummary links for input CollectionExercise then link
   * all SampleSummaries in list to CollectionExercise
   *
   * @param collectionExerciseId the Id of the CollectionExercise to link to
   * @param sampleSummaryIds the list of Ids of the SampleSummaries to be linked
   * @return linkedSummaries the list of CollectionExercises and the linked
   *         SampleSummaries
   */
  List<SampleLink> linkSampleSummaryToCollectionExercise(UUID collectionExerciseId,
      List<UUID> sampleSummaryIds);

  /**
   * Create a collection exercise
   * @param collex the data to create the collection exercise from
   * @return a new CollectionExercise object
   */
  CollectionExercise createCollectionExercise(CollectionExerciseDTO collex);

  /**
   * Gets collection exercise with given exerciseRef and survey (should be no more than 1)
   * @param exerciseRef the exerciseRef (period) of the collection exercise
   * @param survey the survey the collection exercise is associated with
   * @return the collection exercise if it exists, null otherwise
   */
  CollectionExercise  findCollectionExercise(String exerciseRef, SurveyDTO survey);

  /**
   * Gets collection exercise with given exerciseRef and survey uuid (should be no more than 1)
   * @param exerciseRef the exerciseRef (period) of the collection exercise
   * @param surveyId the uuid of the survey the collection exercise is associated with
   * @return the collection exercise if it exists, null otherwise
   */
  CollectionExercise  findCollectionExercise(String exerciseRef, UUID surveyId);

  /**
   * Update a collection exercise
   * @param collex the updated collection exercise
   * @param id the id of the collection exercise to update
   * @return the updated CollectionExercise object
   */
  CollectionExercise updateCollectionExercise(UUID id, CollectionExerciseDTO collex) throws CTPException;

  /**
   * Update a collection exercise
   * @param collex the updated collection exercise
   * @return the updated CollectionExercise object
   */
  CollectionExercise updateCollectionExercise(CollectionExercise collex);

  /**
   * Patch a collection exercise
   * @param id the id of the collection exercise to patch
   * @param collex the patch data
   * @return the patched CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
  CollectionExercise patchCollectionExercise(UUID id, CollectionExerciseDTO collex) throws CTPException;

  /**
   * Delete a collection exercise
   * @param id the id of the collection exercise to delete
   * @return the updated CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
  CollectionExercise deleteCollectionExercise(UUID id) throws CTPException;

  /**
   * Undelete a collection exercise
   * @param id the id of the collection exercise to delete
   * @return the updated CollectionExercise object
   * @throws CTPException thrown if error occurs
   */
  CollectionExercise undeleteCollectionExercise(UUID id) throws CTPException;

  List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state);

  void transitionCollectionExercise(CollectionExercise collex, CollectionExerciseDTO.CollectionExerciseEvent event)
          throws CTPException;


  void maybeSendCiSampleAdded(UUID collexId) throws CTPException;
  void maybeSendCiSampleAdded(CollectionExercise collectionExercise) throws CTPException;

}
