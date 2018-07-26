package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.UUID;

import org.springframework.web.client.RestClientException;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/** Service responsible for dealing with samples */
public interface SurveyService {

  /**
   * Request the delivery of survey from the Survey Service.
   *
   * @param id the survey Id for which to request survey.
   * @return the survey object
   * @throws CTPException if survey service reports error
   */
  SurveyDTO findSurvey(UUID id) throws RestClientException;

  /**
   * Request a survey by reference
   *
   * @param surveyRef surveyRef to request the survey
   * @return the survey object
   * @throws CTPException if survey service reports error
   */
  SurveyDTO findSurveyByRef(String surveyRef) throws RestClientException;
}
