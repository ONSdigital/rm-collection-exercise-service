package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitValidationErrorDTO;

/** The implementation of the SampleService */
@Service
public class SampleService {
  private static final Logger log = LoggerFactory.getLogger(SampleService.class);

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private SampleSvcClient sampleSvcClient;

  /**
   * Method to get all the validation errors for a sample unit
   *
   * @param su a sample unit
   * @return a dto containing all the validation errors
   */
  private static SampleUnitValidationErrorDTO validateSampleUnit(final SampleUnitDTO su) {
    SampleUnitValidationErrorDTO dto = new SampleUnitValidationErrorDTO();

    dto.setSampleUnitRef(su.getSampleUnitRef());
    List<SampleUnitValidationErrorDTO.ValidationError> errors = new ArrayList<>();

    if (su.getCollectionInstrumentId() == null) {
      errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_COLLECTION_INSTRUMENT);
    }
    if (su.getPartyId() == null) {
      errors.add(SampleUnitValidationErrorDTO.ValidationError.MISSING_PARTY);
    }

    SampleUnitValidationErrorDTO.ValidationError[] errorArray =
        errors.toArray(new SampleUnitValidationErrorDTO.ValidationError[errors.size()]);
    dto.setErrors(errorArray);

    return dto;
  }

  /**
   * Get the sample unit validation errors for a given collection exercise
   *
   * @param collectionExercise a collection exercise
   * @return an array of validation errors
   */
  public SampleUnitValidationErrorDTO[] getValidationErrors(
      final CollectionExercise collectionExercise) {

    // first find the sample summary id
    List<SampleLink> sampleLinks =
        sampleLinkRepository.findByCollectionExerciseId(collectionExercise.getId());

    if (sampleLinks.isEmpty()) {
      return new SampleUnitValidationErrorDTO[] {};
    }
    if (sampleLinks.size() > 1) {
      log.warn("More than one sample summary found whilst collecting validation errors");
    }
    SampleLink sampleLink = sampleLinks.get(0);
    UUID sampleSummaryId = sampleLink.getSampleSummaryId();

    // now ask sample service for all samples in a failed state
    SampleUnitDTO[] sampleUnitDTOs =
        sampleSvcClient.requestSampleUnitsForSampleSummary(sampleSummaryId, true);

    List<SampleUnitDTO> sampleUnits = Arrays.asList(sampleUnitDTOs);

    Predicate<SampleUnitDTO> validTest =
        su -> su.getPartyId() == null || su.getCollectionInstrumentId() == null;
    return sampleUnits
        .stream()
        .filter(validTest)
        .map(SampleService::validateSampleUnit)
        .toArray(SampleUnitValidationErrorDTO[]::new);
  }
}
