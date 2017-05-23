package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExerciseSummary;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class CollectionExerciseServiceImpl implements CollectionExerciseService {

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Autowired
  private CaseTypeOverrideRepository caseTypeRepo;

  @Override
  public List<CollectionExerciseSummary> requestCollectionExerciseSummariesForSurvey(Survey survey) {

    List<CollectionExercise> collectionExerciseList = collectRepo.findBySurveySurveyPK(survey.getSurveyPK());

    List<CollectionExerciseSummary> collectionExerciseSummaryList = new ArrayList<>();

    for (CollectionExercise collectionExercise : collectionExerciseList) {
      CollectionExerciseSummary collectionExerciseSummary = new CollectionExerciseSummary();
      collectionExerciseSummary.setId(collectionExercise.getId());
      collectionExerciseSummary.setName(collectionExercise.getName());
      collectionExerciseSummary.setScheduledExecution(collectionExercise.getScheduledExecutionDateTime());

      collectionExerciseSummaryList.add(collectionExerciseSummary);

    }

    return collectionExerciseSummaryList;
  }

  @Override
  public CollectionExercise requestCollectionExercise(UUID id) {

    return collectRepo.findOneById(id);
  }

  @Override
  public List<CaseType> getCaseTypesForCollectionExercise(Integer collectionExercisePK) {

    return caseTypeRepo.findByExerciseFK(collectionExercisePK);
  }
}
