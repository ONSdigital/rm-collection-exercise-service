package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.SurveyRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;

import java.util.UUID;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class SurveyServiceImpl implements SurveyService {

  @Autowired
  private SurveyRepository surveyRepo;

  @Override
  public Survey findSurvey(UUID id) {
    return surveyRepo.findById(id);
  }
  
  @Override
  public Survey findSurveyByFK(int surveyFK) {
    return surveyRepo.findOne(surveyFK);
  }
}
