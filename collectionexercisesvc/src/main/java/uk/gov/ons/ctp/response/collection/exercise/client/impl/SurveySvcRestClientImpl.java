package uk.gov.ons.ctp.response.collection.exercise.client.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.client.SurveySvcClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.response.survey.representation.SurveyClassifierDTO;
import uk.gov.ons.response.survey.representation.SurveyClassifierTypeDTO;

/**
 * HTTP RestClient implementation for calls to the Survey service
 *
 */
@Component
public class SurveySvcRestClientImpl implements SurveySvcClient {

  @Autowired
  private AppConfig appConfig;

  @Autowired
  @Qualifier("surveySvc")
  private RestClient surveySvcClientRestTemplate;

  @Override
  public List<SurveyClassifierDTO> requestClassifierTypeSelectors(final UUID surveyId) {
    return surveySvcClientRestTemplate.getResources(appConfig.getSurveySvc().getRequestClassifierTypesListPath(),
        SurveyClassifierDTO[].class, surveyId);
  }

  @Override
  public SurveyClassifierTypeDTO requestClassifierTypeSelector(final UUID surveyId, final UUID classifierType) {
    return surveySvcClientRestTemplate.getResource(appConfig.getSurveySvc().getRequestClassifierTypesPath(),
        SurveyClassifierTypeDTO.class, surveyId, classifierType);
  }
}
