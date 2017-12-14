package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.*;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.domain.Survey;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SurveyRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

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

  @Autowired
  private SampleLinkRepository sampleLinkRepository;

  @Override
  public List<CollectionExercise> findCollectionExercisesForSurvey(Survey survey) {

    return collectRepo.findBySurveySurveyPK(survey.getSurveyPK());
  }

  @Override
  public CollectionExercise findCollectionExercise(UUID id) {

    return collectRepo.findOneById(id);
  }

  @Override
  public List<SampleLink> findLinkedSampleSummaries(UUID id) {
    return sampleLinkRepository.findByCollectionExerciseId(id);
  }

  @Override
  public List<CollectionExercise> findAllCollectionExercise() {
    return collectRepo.findAll();
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
   *
   * @param caseTypeDefaultList List of caseTypeDefaults
   * @param caseTypeOverrideList List of caseTypeOverrides
   * @return Collection<CaseType> Collection of CaseTypes
   */
  public Collection<CaseType> createCaseTypeList(List<? extends CaseType> caseTypeDefaultList,
      List<? extends CaseType> caseTypeOverrideList) {

    Map<String, CaseType> defaultMap = new HashMap<>();

    for (CaseType caseTypeDefault : caseTypeDefaultList) {
      defaultMap.put(caseTypeDefault.getSampleUnitTypeFK(), caseTypeDefault);
    }

    for (CaseType caseTypeOverride : caseTypeOverrideList) {
      defaultMap.put(caseTypeOverride.getSampleUnitTypeFK(), caseTypeOverride);
    }

    return defaultMap.values();
  }

  /**
   * Delete existing SampleSummary links for input CollectionExercise then link
   * all SampleSummaries in list to CollectionExercise
   *
   * @param collectionExerciseId the Id of the CollectionExercise to link to
   * @param sampleSummaryIds the list of Ids of the SampleSummaries to be linked
   * @return linkedSummaries the list of CollectionExercises and the linked
   *         SampleSummaries
   */
  @Transactional
  public List<SampleLink> linkSampleSummaryToCollectionExercise(UUID collectionExerciseId,
      List<UUID> sampleSummaryIds) {

    sampleLinkRepository.deleteByCollectionExerciseId(collectionExerciseId);
    List<SampleLink> linkedSummaries = new ArrayList<SampleLink>();
    for (UUID summaryId : sampleSummaryIds) {
      linkedSummaries.add(createLink(summaryId, collectionExerciseId));
    }

    return linkedSummaries;
  }

  @Override
  public CollectionExercise createCollectionExercise(CollectionExerciseDTO collex) {
      Survey survey = surveyRepo.findById(UUID.fromString(collex.getSurveyId()));
      CollectionExercise collectionExercise = new CollectionExercise();

      collectionExercise.setName(collex.getName());
      collectionExercise.setUserDescription(collex.getUserDescription());
      collectionExercise.setExerciseRef(collex.getExerciseRef());
      collectionExercise.setCreated(new Timestamp(new Date().getTime()));
      collectionExercise.setId(UUID.randomUUID());
      collectionExercise.setSurvey(survey);
      collectionExercise.setState(CollectionExerciseDTO.CollectionExerciseState.INIT);

      return this.collectRepo.save(collectionExercise);
  }

  @Override
  public CollectionExercise findCollectionExercise(String exerciseRef, Survey survey) {
      List<CollectionExercise> existing = this.collectRepo.findByExerciseRefAndSurveySurveyPK(
              exerciseRef,
              survey.getSurveyPK());

      switch(existing.size()){
        case 0:
          return null;
        default:
          return existing.get(0);
      }
  }

   @Override
   public CollectionExercise findCollectionExercise(String exerciseRef, UUID surveyId) {
      List<CollectionExercise> existing = this.collectRepo.findByExerciseRefAndSurveyId(exerciseRef, surveyId);

       switch(existing.size()){
           case 0:
               return null;
           default:
               return existing.get(0);
       }
   }

    @Override
  public CollectionExercise updateCollectionExercise(UUID id, CollectionExerciseDTO collexDto)  throws CTPException {
    CollectionExercise existing = findCollectionExercise(id);

    if (existing == null){
      throw new CTPException(
              CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("Collection exercise with id %s does not exist", id));
    } else {
        // If either the survey id or the period has changed, then the unique identifier for this collection exercise
        // has changed so we need to re-validate uniqueness
        UUID surveyUuid = UUID.fromString(collexDto.getSurveyId());
        String period = collexDto.getExerciseRef();
        if (existing.getSurvey().getId().equals(surveyUuid) == false
                || existing.getExerciseRef().equals(period) == false){
            CollectionExercise otherExisting = findCollectionExercise(period, surveyUuid);

            if (otherExisting != null && otherExisting.getId().equals(existing.getId()) == false){
                throw new CTPException(
                        CTPException.Fault.RESOURCE_VERSION_CONFLICT,
                        String.format("A collection exercise with period %s and id %s already exists.",
                                period,
                                surveyUuid));
            }
        }
        Survey survey = this.surveyRepo.findById(surveyUuid);

        if (survey == null) {
            throw new CTPException(
                    CTPException.Fault.BAD_REQUEST,
                    String.format("Survey %s does not exist", surveyUuid));
        } else {
            existing.setUserDescription(collexDto.getUserDescription());
            existing.setName(collexDto.getName());
            existing.setExerciseRef(collexDto.getExerciseRef());
            existing.setSurvey(survey);

            return this.collectRepo.save(existing);
        }
    }
  }

    private CollectionExercise updateCollectionExerciseDeleted(UUID id, boolean deleted) throws CTPException {
        CollectionExercise collex = findCollectionExercise(id);

        if (collex == null){
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, String.format("Collection exercise %s does not exists", id));
        } else {
            collex.setDeleted(deleted);

            return this.collectRepo.save(collex);
        }
    }

    @Override
    public CollectionExercise deleteCollectionExercise(UUID id) throws CTPException {
      return updateCollectionExerciseDeleted(id, true);
    }

    @Override
    public CollectionExercise undeleteCollectionExercise(UUID id) throws CTPException {
        return updateCollectionExerciseDeleted(id, false);
    }

    /**
   * Links a sample summary to a collection exercise and stores in db
   *
   * @param sampleSummaryId the Id of the Sample summary to be linked
   * @param collectionExerciseId the Id of the Sample summary to be linked
   * @return sampleLink stored in database
   */
  private SampleLink createLink(UUID sampleSummaryId, UUID collectionExerciseId) {
    SampleLink sampleLink = new SampleLink();
    sampleLink.setSampleSummaryId(sampleSummaryId);
    sampleLink.setCollectionExerciseId(collectionExerciseId);
    return sampleLinkRepository.saveAndFlush(sampleLink);
  }

}
