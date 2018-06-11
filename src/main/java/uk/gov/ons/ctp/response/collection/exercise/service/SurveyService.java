package uk.gov.ons.ctp.response.collection.exercise.service;

import org.springframework.web.client.RestClientException;
import uk.gov.ons.response.survey.representation.SurveyDTO;

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
  SurveyDTO findSurvey(UUID id) throws RestClientException;

  /**
   * Request a survey by reference
   * @param surveyRef surveyRef to request the survey
   * @return the survey object
   */
  SurveyDTO findSurveyByRef(String surveyRef) throws RestClientException;

}
