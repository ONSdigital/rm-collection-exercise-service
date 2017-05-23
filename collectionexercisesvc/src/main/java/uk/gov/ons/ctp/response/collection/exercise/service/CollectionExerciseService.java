package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExerciseSummary;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;

import java.util.List;
import java.util.UUID;

/**
 * Service responsible for dealing with collection exercises
 *
 */
public interface CollectionExerciseService {


  /**
   * Request a list of surveys associated to a collection exercise Id from the Collection Exercise Service
   *
   * @param survey the survey for which to request collection exercises
   *          units.
   * @return the associated surveys.
   */
  List<CollectionExerciseSummary> requestCollectionExerciseSummariesForSurvey(final Survey survey);


  /**
   * Request a collection exercise associated to a collection exercise Id from the Collection Exercise Service
   *
   * @param id the collection exercise Id for which to request collection exercise
   *          units.
   * @return the associated collection exercise.
   */
  CollectionExercise requestCollectionExercise(final UUID id);


  List<CaseType> getCaseTypesForCollectionExercise(final Integer collectionExercisePK);


}
