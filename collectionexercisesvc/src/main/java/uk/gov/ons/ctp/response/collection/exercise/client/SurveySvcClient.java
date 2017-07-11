package uk.gov.ons.ctp.response.collection.exercise.client;

import java.util.List;
import java.util.UUID;

import org.springframework.web.client.RestClientException;

import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

/**
 * Service responsible for making client calls to the Survey service
 *
 */
public interface SurveySvcClient {

  /**
   * Get classifier type selectors for Survey by UUID.
   *
   * @param surveyId UUID for which to request classifiers.
   * @return List of SurveyClassifierDTO classifier selectors.
   * @throws RestClientException something went wrong making http call.
   *
   */
  List<SurveyClassifierDTO> requestClassifierTypeSelectors(UUID surveyId) throws RestClientException;

  /**
   * Get classifier type selector for Survey UUID and ClassifierType UUID.
   *
   * @param surveyId UUID for which to request classifiers.
   * @param classifierType UUID for classifier type.
   * @return SurveyClassifierTypeDTO details of selector type requested.
   * @throws RestClientException something went wrong making http call.
   *
   */
  SurveyClassifierTypeDTO requestClassifierTypeSelector(UUID surveyId, UUID classifierType) throws RestClientException;

}
