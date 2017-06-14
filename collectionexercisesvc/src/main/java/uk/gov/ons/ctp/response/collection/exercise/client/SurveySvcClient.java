package uk.gov.ons.ctp.response.collection.exercise.client;

import java.util.List;
import java.util.UUID;

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
   */
  List<SurveyClassifierDTO> requestClassifierTypeSelectors(final UUID surveyId);

  /**
   * Get classifier type selector for Survey UUID and ClassifierType UUID.
   *
   * @param surveyId UUID for which to request classifiers.
   * @param classifierType UUID for classifier type.
   * @return SurveyClassifierTypeDTO details of selector type requested.
   *
   */
  SurveyClassifierTypeDTO requestClassifierTypeSelector(final UUID surveyId, final UUID classifierType);

}
