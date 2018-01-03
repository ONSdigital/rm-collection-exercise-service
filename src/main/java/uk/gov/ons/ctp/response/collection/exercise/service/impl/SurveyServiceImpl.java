package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.SurveyRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Qualifier("database")
public class SurveyServiceImpl implements SurveyService {

  @Autowired
  private SurveyRepository surveyRepo;

  @Override
  public SurveyDTO findSurvey(UUID id) {
    SurveyDTO result = null;
    Survey survey = surveyRepo.findById(id);

    if (survey != null) {
      result = new SurveyDTO();

      result.setId(survey.getId().toString());
      result.setSurveyRef(survey.getSurveyRef());
    }

    return result;
  }

  @Override
  public SurveyDTO findSurveyByRef(String surveyRef) {
    SurveyDTO result = null;
    Survey survey = surveyRepo.findBySurveyRef(surveyRef);

    if (survey != null) {
      result = new SurveyDTO();

      result.setId(survey.getId().toString());
      result.setSurveyRef(survey.getSurveyRef());
    }

    return result;
  }

}
