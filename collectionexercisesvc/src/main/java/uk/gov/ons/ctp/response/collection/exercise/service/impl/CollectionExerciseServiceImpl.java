package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.*;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SurveyRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CaseTypeDTO;
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
  private CaseTypeOverrideRepository caseTypeOverrideRepo;

  @Autowired
  private CaseTypeDefaultRepository caseTypeDefaultRepo;

  @Autowired
  private SurveyRepository surveyRepo;

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
  public List<CaseTypeDTO> getCaseTypesDTOList(CollectionExercise collectionExercise) {

    Survey survey = surveyRepo.findById(collectionExercise.getSurvey().getId());

    List<CaseTypeDefault> caseTypeDefaultList = caseTypeDefaultRepo.findBySurveyFK(survey.getSurveyPK());

    List<CaseTypeOverride> caseTypeOverrideList = caseTypeOverrideRepo.findByExerciseFK(collectionExercise.getExercisePK());

    List<CaseTypeDTO> caseTypeDTOList = new ArrayList<>();

    //For each caseTypeDefault, loop through each caseTypeOverride and check if sampleUnitTypeFK matches
    //If so, add caseTypeDTO from caseTypeOverride else add caseTypeDTO from caseTypeDefault.
    for (CaseTypeDefault caseTypeDefault : caseTypeDefaultList) {

      CaseTypeDTO caseTypeDTO = new CaseTypeDTO();
      caseTypeDTO.setSampleUnitType(caseTypeDefault.getSampleUnitTypeFK());
      caseTypeDTO.setActionPlanId(caseTypeDefault.getActionPlanId());

      for (CaseTypeOverride caseTypeOverride : caseTypeOverrideList) {

        if (caseTypeDefault.getSampleUnitTypeFK().equals(caseTypeOverride.getSampleUnitTypeFK())) {
          caseTypeDTO.setSampleUnitType(caseTypeOverride.getSampleUnitTypeFK());
          caseTypeDTO.setActionPlanId(caseTypeOverride.getActionPlanId());
          break;
        }

      }

      caseTypeDTOList.add(caseTypeDTO);

    }

    return caseTypeDTOList;
  }
}
