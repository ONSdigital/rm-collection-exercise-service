package uk.gov.ons.ctp.response.collection.exercise.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.domain.CaseTypeOverride;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.repository.CaseTypeOverrideRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.CaseTypeOverrideService;

@Slf4j
@Service
public class CaseTypeOverrideServiceImpl implements CaseTypeOverrideService {
  private final CaseTypeOverrideRepository caseTypeOverrideRepository;

  public CaseTypeOverrideServiceImpl(final CaseTypeOverrideRepository caseTypeOverrideRepository) {
    this.caseTypeOverrideRepository = caseTypeOverrideRepository;
  }

  @Override
  public CaseTypeOverride getCaseTypeOverride(
      final CollectionExercise collectionExercise, final String sampleUnitType)
      throws CTPException {
    final CaseTypeOverride caseTypeOverride =
        caseTypeOverrideRepository.findTopByExerciseFKAndSampleUnitTypeFK(
            collectionExercise.getExercisePK(), sampleUnitType);

    if (caseTypeOverride == null) {
      log.warn(
          "Business or business individual override action plans do not exist,"
              + " CollectionExerciseId: {}",
          collectionExercise.getId());

      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND,
          String.format(
              "Override action plans do not exist for collection exercise %s",
              collectionExercise.getId()));
    }

    return caseTypeOverride;
  }
}
