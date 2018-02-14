package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseType;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeDefault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeDefaultRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SurveyService;
import uk.gov.ons.response.survey.representation.SurveyDTO;

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
  private SurveyService surveyService;

  @Autowired
  private SampleLinkRepository sampleLinkRepository;

  @Autowired
  @Qualifier("collectionExercise")
  private StateTransitionManager<CollectionExerciseDTO.CollectionExerciseState, CollectionExerciseDTO.CollectionExerciseEvent> collectionExerciseTransitionState;

  @Override
  public List<CollectionExercise> findCollectionExercisesForSurvey(SurveyDTO survey) {
    return this.collectRepo.findBySurveyId(UUID.fromString(survey.getId()));
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
  public CollectionExercise findCollectionExercise(String surveyRef, String exerciseRef) {
      CollectionExercise collex = null;
      SurveyDTO survey = this.surveyService.findSurveyByRef(surveyRef);

      if (survey != null){
          collex = findCollectionExercise(exerciseRef, survey);
      }

      return collex;
  }

  @Override
  public Collection<CaseType> getCaseTypesList(CollectionExercise collectionExercise) {

    List<CaseTypeDefault> caseTypeDefaultList = caseTypeDefaultRepo.findBySurveyId(collectionExercise.getSurveyId());

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

    /**
     * Sets the values in a supplied collection exercise from a supplied DTO.
     * WARNING: Mutates collection exercise
     * @param collex the dto containing the data
     * @param collectionExercise the collection exercise to apply the value from the dto to
     */
  private void setCollectionExerciseFromDto(CollectionExerciseDTO collex, CollectionExercise collectionExercise) {
      collectionExercise.setName(collex.getName());
      collectionExercise.setUserDescription(collex.getUserDescription());
      collectionExercise.setExerciseRef(collex.getExerciseRef());
      collectionExercise.setSurveyId(UUID.fromString(collex.getSurveyId()));

      // In the strictest sense, some of these dates are mandatory fields for collection exercises.  However as they
      // are not supplied at creation time, but later as "events" we will allow them to be null
      if (collex.getScheduledStartDateTime() != null) {
          collectionExercise.setScheduledStartDateTime(new Timestamp(collex.getScheduledStartDateTime().getTime()));
      }
      if (collex.getScheduledEndDateTime() != null) {
          collectionExercise.setScheduledEndDateTime(new Timestamp(collex.getScheduledEndDateTime().getTime()));
      }
      if (collex.getScheduledExecutionDateTime() != null) {
          collectionExercise.setScheduledExecutionDateTime(
                  new Timestamp(collex.getScheduledExecutionDateTime().getTime()));
      }
      if (collex.getActualExecutionDateTime() != null) {
          collectionExercise.setActualExecutionDateTime(new Timestamp(collex.getActualExecutionDateTime().getTime()));
      }
      if (collex.getActualPublishDateTime() != null) {
          collectionExercise.setActualPublishDateTime(new Timestamp(collex.getActualPublishDateTime().getTime()));
      }
  }

  @Override
  public CollectionExercise createCollectionExercise(CollectionExerciseDTO collex) {
      CollectionExercise collectionExercise = new CollectionExercise();

      setCollectionExerciseFromDto(collex, collectionExercise);

      collectionExercise.setState(CollectionExerciseDTO.CollectionExerciseState.CREATED);
      collectionExercise.setCreated(new Timestamp(new Date().getTime()));
      collectionExercise.setId(UUID.randomUUID());

      return this.collectRepo.save(collectionExercise);
  }

  @Override
  public CollectionExercise findCollectionExercise(String exerciseRef, SurveyDTO survey) {
      List<CollectionExercise> existing = this.collectRepo.findByExerciseRefAndSurveyId(
              exerciseRef,
              UUID.fromString(survey.getId()));

      switch (existing.size()) {
        case 0:
          return null;
        default:
          return existing.get(0);
      }
  }

   @Override
   public CollectionExercise findCollectionExercise(String exerciseRef, UUID surveyId) {
      List<CollectionExercise> existing = this.collectRepo.findByExerciseRefAndSurveyId(exerciseRef, surveyId);

       switch (existing.size()) {
           case 0:
               return null;
           default:
               return existing.get(0);
       }
   }

   @Override
   public CollectionExercise patchCollectionExercise(UUID id, CollectionExerciseDTO patchData) throws CTPException {
      CollectionExercise collex = findCollectionExercise(id);

      if (collex == null) {
          throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                  String.format("Collection exercise %s not found", id));
      } else {
          String proposedPeriod = patchData.getExerciseRef() == null
                  ? collex.getExerciseRef()
                  : patchData.getExerciseRef();
          UUID proposedSurvey = patchData.getSurveyId() == null
                  ? collex.getSurveyId()
                  : UUID.fromString(patchData.getSurveyId());

          // If period/survey not supplied in patchData then this call will trivially return
          validateUniqueness(collex, proposedPeriod, proposedSurvey);

          if (!StringUtils.isBlank(patchData.getSurveyId())) {
              UUID surveyId = UUID.fromString(patchData.getSurveyId());

              SurveyDTO survey = this.surveyService.findSurvey(surveyId);

              if (survey == null) {
                  throw new CTPException(CTPException.Fault.BAD_REQUEST,
                          String.format("Survey %s does not exist", surveyId));
              } else {
                  collex.setSurveyId(surveyId);
              }
          }

          if (!StringUtils.isBlank(patchData.getExerciseRef())) {
              collex.setExerciseRef(patchData.getExerciseRef());
          }
          if (!StringUtils.isBlank(patchData.getName())) {
              collex.setName(patchData.getName());
          }
          if (!StringUtils.isBlank(patchData.getUserDescription())) {
              collex.setUserDescription(patchData.getUserDescription());
          }
          if (patchData.getScheduledStartDateTime() != null){
              collex.setScheduledStartDateTime(new Timestamp(patchData.getScheduledStartDateTime().getTime()));
          }

          collex.setUpdated(new Timestamp(new Date().getTime()));

          return this.collectRepo.save(collex);
      }
   }

    /**
     * This method checks whether the supplied CollectionExercise (existing) can change it's period to candidatePeriod
     * and it's survey to candidateSurvey without breaching the uniqueness constraint on those fields
     * @param existing the collection exercise that is to be updated
     * @param candidatePeriod the proposed new value for the period (exerciseRef)
     * @param candidateSurvey the proposed new value for the survey
     * @throws CTPException thrown if there is an existing different collection exercise that already uses the proposed
     *                      combination of period and survey
     */
   private void validateUniqueness(CollectionExercise existing, String candidatePeriod, UUID candidateSurvey)
           throws CTPException {
       if (!existing.getSurveyId().equals(candidateSurvey)
               || !existing.getExerciseRef().equals(candidatePeriod)) {
           CollectionExercise otherExisting = findCollectionExercise(candidatePeriod, candidateSurvey);

           if (otherExisting != null && !otherExisting.getId().equals(existing.getId())) {
               throw new CTPException(
                       CTPException.Fault.RESOURCE_VERSION_CONFLICT,
                       String.format("A collection exercise with period %s and id %s already exists.",
                               candidatePeriod,
                               candidateSurvey));
           }
       }
   }

    @Override
  public CollectionExercise updateCollectionExercise(UUID id, CollectionExerciseDTO collexDto)  throws CTPException {
    CollectionExercise existing = findCollectionExercise(id);

    if (existing == null) {
      throw new CTPException(
              CTPException.Fault.RESOURCE_NOT_FOUND,
              String.format("Collection exercise with id %s does not exist", id));
    } else {
        UUID surveyUuid = UUID.fromString(collexDto.getSurveyId());
        String period = collexDto.getExerciseRef();

        // This will throw exception if period & surveyId are not unique
        validateUniqueness(existing, period, surveyUuid);

        SurveyDTO survey = this.surveyService.findSurvey(surveyUuid);

        if (survey == null) {
            throw new CTPException(
                    CTPException.Fault.BAD_REQUEST,
                    String.format("Survey %s does not exist", surveyUuid));
        } else {
            setCollectionExerciseFromDto(collexDto, existing);
            existing.setUpdated(new Timestamp(new Date().getTime()));

            return this.collectRepo.save(existing);
        }
    }
  }

    @Override
    public CollectionExercise updateCollectionExercise(final CollectionExercise collex) {
       return this.collectRepo.save(collex);
    }


    /**
     * Utility method to set the deleted flag for a collection exercise
     * @param id the uuid of the collection exercise to update
     * @param deleted true if the collection exercise is to be marked as deleted, false otherwise
     * @return 200 if success, 404 if not found
     * @throws CTPException thrown if specified collection exercise does not exist
     */
  private CollectionExercise updateCollectionExerciseDeleted(UUID id, boolean deleted) throws CTPException {
      CollectionExercise collex = findCollectionExercise(id);

      if (collex == null) {
          throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND,
                  String.format("Collection exercise %s does not exists", id));
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

    @Override
    public List<CollectionExercise> findByState(CollectionExerciseDTO.CollectionExerciseState state) {
        return collectRepo.findByState(state);
    }

    @Override
    public void transitionCollectionExercise(CollectionExercise collex,
                                             CollectionExerciseDTO.CollectionExerciseEvent event) throws CTPException {
        CollectionExerciseDTO.CollectionExerciseState oldState = collex.getState();
        CollectionExerciseDTO.CollectionExerciseState newState =
                collectionExerciseTransitionState.transition(collex.getState(), event);
        if (oldState != newState){
            collex.setState(newState);
            this.collectRepo.saveAndFlush(collex);
        }
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
