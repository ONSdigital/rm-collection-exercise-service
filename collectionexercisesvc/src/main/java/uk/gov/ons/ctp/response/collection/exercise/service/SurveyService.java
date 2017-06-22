package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;

import java.util.UUID;

/**
 * Service responsible for dealing with samples
 *
 */
public interface SurveyService {

  /**
   * Request the delivery of survey from the Survey Service.
   *
   * @param id the survey Id for which to request survey.
   * @return the survey object
   */
  Survey findSurvey(final UUID id);

  /**
   * Request the delivery of survey from the Survey Service.
   *
   * @param surveyFK the survey FK for which to request survey.
   * @return the survey object
   */
  Survey findSurveyByFK(int surveyFK);

}
