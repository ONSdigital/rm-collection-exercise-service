package uk.gov.ons.ctp.response.collection.exercise.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleUnitsRequestDTO;
import uk.gov.ons.ctp.response.collection.exercise.message.SampleSummaryActivationPublisher;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.change.SampleSummaryDistributionException;

@SuppressWarnings("LoggingSimilarMessage")
@Service
public class SampleSummaryService {

  private static final Logger log = LoggerFactory.getLogger(SampleSummaryService.class);

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private CollectionExerciseRepository collectionExerciseRepository;

  @Autowired private SampleSummaryActivationPublisher sampleSummaryActivationPublisher;

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Autowired private EventRepository eventRepository;

  @Autowired private SampleSvcClient sampleSvcClient;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void activateSamples(UUID collectionExerciseId) {

    CollectionExercise collectionExercise =
        collectionExerciseRepository.findOneById(collectionExerciseId);

    // first transition to executed state
    executionStarted(collectionExercise);
    UUID surveyId = collectionExercise.getSurveyId();

    List<SampleLink> sampleLinks =
        sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId);
    List<UUID> sampleSummaryIdList =
        sampleLinks.stream().map(SampleLink::getSampleSummaryId).collect(Collectors.toList());

    if (sampleSummaryIdList.size() > 1) {
      log.error(
          "Multiple sample summaries detected during enrichment phase",
          kv("numberOfSampleSummaries", sampleSummaryIdList.size()),
          kv("collectionExerciseId", collectionExerciseId));
    }
    // in rasrm business there can only ever be one sample summary per collection exercise
    UUID sampleSummaryId = sampleSummaryIdList.get(0);

    setSampleUnitCount(collectionExercise, sampleSummaryIdList);

    sampleSummaryActivationPublisher.sendSampleSummaryActivation(
        collectionExerciseId, sampleSummaryId, surveyId);

    // now transition to executed complete
    executionCompleted(collectionExercise);
  }

  private void setSampleUnitCount(
      CollectionExercise collectionExercise, List<UUID> sampleSummaryIdList) {
    SampleUnitsRequestDTO responseDTO = sampleSvcClient.getSampleUnitCount(sampleSummaryIdList);

    Integer sampleUnitCount = responseDTO.getSampleUnitsTotal();
    collectionExercise.setSampleSize(sampleUnitCount);
    log.info(
        "Sample Unit count received",
        kv("collectionExerciseId", collectionExercise.getId()),
        kv("sampleUnitCount", sampleUnitCount));
    collectionExerciseRepository.saveAndFlush(collectionExercise);
  }

  private void executionStarted(CollectionExercise collectionExercise) {
    // transition collection exercise to executed state
    try {
      log.info("transitioning state", kv("collectionExerciseId", collectionExercise.getId()));
      collectionExerciseService.transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTE);
    } catch (CTPException e) {
      log.error("unable to transition collection exercise", e);
    }
  }

  private void executionCompleted(CollectionExercise collectionExercise) {
    // transition collection exercise to executed state
    try {
      log.info("transitioning state", kv("collectionExerciseId", collectionExercise.getId()));
      collectionExerciseService.transitionCollectionExercise(
          collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EXECUTION_COMPLETE);
    } catch (CTPException e) {
      log.error("unable to transition collection exercise", e);
    }
  }

  /**
   * Transitions the state of a collection exercise depending on the outcome of the validation of
   * the sample summary
   *
   * @param valid true if sample summary valid and enriched, false otherwise
   * @param collectionExerciseId the id of the collection exercise
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void sampleSummaryValidated(boolean valid, UUID collectionExerciseId)
      throws SampleSummaryValidationException {
    CollectionExercise collectionExercise =
        collectionExerciseRepository.findOneById(collectionExerciseId);
    CollectionExerciseDTO.CollectionExerciseEvent event;
    if (valid) {
      log.info("collection exercise valid", kv("collectionExerciseId", collectionExerciseId));
      event = CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE;
    } else {
      log.info("collection exercise invalid", kv("collectionExerciseId", collectionExerciseId));
      event = CollectionExerciseDTO.CollectionExerciseEvent.INVALIDATE;
    }
    try {
      log.info("transitioning state", kv("collectionExerciseId", collectionExerciseId));
      collectionExerciseService.transitionCollectionExercise(collectionExercise, event);
      log.info(
          "collection exercise transition successful",
          kv("collectionExerciseId", collectionExerciseId),
          kv("valid", valid));
    } catch (CTPException e) {
      log.error("unable to transition collection exercise", e);
      throw new SampleSummaryValidationException(e);
    }
  }

  /**
   * Transitions the state of a collection exercise depending on the outcome of the distribution of
   * the sample summary
   *
   * @param distributed true if sample summary sent to case false otherwise
   * @param collectionExerciseId the id of the collection exercise
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public void sampleSummaryDistributed(boolean distributed, UUID collectionExerciseId)
      throws SampleSummaryDistributionException {
    CollectionExercise collectionExercise =
        collectionExerciseRepository.findOneById(collectionExerciseId);
    CollectionExerciseDTO.CollectionExerciseEvent event;
    if (distributed) {

      try {
        log.info(
            "collection exercise distributed, transitioning to READY_FOR_LIVE",
            kv("collectionExerciseId", collectionExerciseId));
        // All sample units published, set exercise state to READY_FOR_LIVE
        collectionExerciseService.transitionCollectionExercise(
            collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH);
        log.info(
            "collection exercise transitioned to READY_FOR_LIVE successfully",
            kv("collectionExerciseId", collectionExerciseId));
      } catch (CTPException e) {
        log.error("Failed to transition collection exercise to READY_FOR_LIVE", e);
        throw new SampleSummaryDistributionException(e);
      }
    } else {
      log.error(
          "collection exercise failed distribution. Manual intervention required",
          kv("collectionExerciseId", collectionExerciseId));
    }
  }
}
