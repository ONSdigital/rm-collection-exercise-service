package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExerciseSummary;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the SampleService
 *
 */
@Service
@Slf4j
public class CollectionExerciseServiceImpl implements CollectionExerciseService {

  @Autowired
  private CollectionExerciseRepository collectRepo;

  @Override
  public List<CollectionExerciseSummary> requestCollectionExerciseSummariesForSurvey(Survey survey) {

    List<CollectionExercise> collectionExerciseList = collectRepo.findBySurveySurveyPK(survey.getSurveyPK());

    List<CollectionExerciseSummary> collectionExerciseSummaryList = new ArrayList<>();

    for (CollectionExercise collex : collectionExerciseList) {
      CollectionExerciseSummary collectionExerciseSummary = new CollectionExerciseSummary();
      collectionExerciseSummary.setId(collex.getId());
      collectionExerciseSummary.setName(survey.getSurveyRef()); //TODO: Where is name taken from?
      collectionExerciseSummary.setScheduledExecution(collex.getScheduledExecutionDateTime());

      collectionExerciseSummaryList.add(collectionExerciseSummary);

    }

    return collectionExerciseSummaryList;
  }

  @Override
  public CollectionExercise requestCollectionExercise(String id) {

    CollectionExercise collectionExercise = collectRepo.findOne(id);

    return collectionExercise;
  }
}
