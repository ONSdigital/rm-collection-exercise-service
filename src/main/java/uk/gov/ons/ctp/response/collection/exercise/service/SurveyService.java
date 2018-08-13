package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.UUID;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Service responsible for dealing with samples */
public interface SurveyService {

  /**
   * Request the delivery of survey from the Survey Service.
   *
   * @param id the survey Id for which to request survey.
   * @return the survey object
   */
  SurveyDTO findSurvey(UUID id);

  /**
   * Request a survey by reference
   *
   * @param surveyRef surveyRef to request the survey
   * @return the survey object
   */
  SurveyDTO findSurveyByRef(String surveyRef);

  /**
   * Get Survey linked to collection exercise.
   *
   * @param collex the collection exercise.
   * @return the survey object
   * @throws CTPException on not found
   */
  SurveyDTO getSurveyForCollectionExercise(final CollectionExercise collex) throws CTPException;
}
