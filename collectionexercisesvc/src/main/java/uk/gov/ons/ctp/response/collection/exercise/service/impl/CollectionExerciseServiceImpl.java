package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SurveyRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public List<CollectionExercise> findCollectionExercisesForSurvey(Survey survey) {

    return collectRepo.findBySurveySurveyPK(survey.getSurveyPK());
  }

  @Override
  public CollectionExercise findCollectionExercise(UUID id) {

    return collectRepo.findOneById(id);
  }

  @Override
  public Collection<CaseType> getCaseTypesList(CollectionExercise collectionExercise) {

    Survey survey = surveyRepo.findById(collectionExercise.getSurvey().getId());

    List<CaseTypeDefault> caseTypeDefaultList = caseTypeDefaultRepo.findBySurveyFK(survey.getSurveyPK());

    List<CaseTypeOverride> caseTypeOverrideList = caseTypeOverrideRepo
            .findByExerciseFK(collectionExercise.getExercisePK());

    return createCaseTypeList(caseTypeDefaultList, caseTypeOverrideList);
  }

  /**
   * Creates a Collection of CaseTypes
   * @param caseTypeDefaultList List of caseTypeDefaults
   * @param caseTypeOverrideList List of caseTypeOverrides
   * @return Collection<CaseType> Collection of CaseTypes
   */
  Collection<CaseType> createCaseTypeList(List<? extends CaseType> caseTypeDefaultList, List<? extends CaseType>
          caseTypeOverrideList) {

    Map<String, CaseType> defaultMap = new HashMap<>();

    for (CaseType caseTypeDefault : caseTypeDefaultList) {
      defaultMap.put(caseTypeDefault.getSampleUnitTypeFK(), caseTypeDefault);
    }

    for (CaseType caseTypeOverride : caseTypeOverrideList) {
      defaultMap.put(caseTypeOverride.getSampleUnitTypeFK(), caseTypeOverride);
    }

    return defaultMap.values();
  }

}
