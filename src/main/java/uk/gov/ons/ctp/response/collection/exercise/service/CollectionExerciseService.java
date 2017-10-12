package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.representation.LinkSampleSummaryOutputDTO;

/**
 * Service responsible for dealing with collection exercises
 *
 */
public interface CollectionExerciseService {


  /**
   * Find a list of surveys associated to a collection exercise Id from the Collection Exercise Service
   *
   * @param survey the survey for which to find collection exercises
   * @return the associated surveys.
   */
  List<CollectionExercise> findCollectionExercisesForSurvey(Survey survey);


  /**
   * Find a collection exercise associated to a collection exercise Id from the Collection Exercise Service
   *
   * @param id the collection exercise Id for which to find collection exercise
   * @return the associated collection exercise.
   */
  CollectionExercise findCollectionExercise(UUID id);
  
  /**
   * find a list of all sample summary linked to a collection exercise
   * 
   * @param id the collection exercise Id to find the linked sample summaries for
   * @return list of linked sample summary
   */
  List<SampleLink> findLinkedSampleSummaries(UUID id);

  /**
   * Find case types associated to a collection exercise from the Collection Exercise Service
   *
   * @param collectionExercise the collection exercise for which to find case types
   * @return the associated case type DTOs.
   */
  Collection<CaseType> getCaseTypesList(CollectionExercise collectionExercise);
  
  List<LinkSampleSummaryOutputDTO> linkSampleSummaryToCollectionExercise(UUID collectionExerciseId, List<UUID> sampleSummaryId);
  
  void createLink(UUID sampleSummaryId, UUID collectionExerciseId);



}
