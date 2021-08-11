package uk.gov.ons.ctp.response.collection.exercise.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.ctp.response.collection.exercise.client.SampleSvcClient;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

@Service
public class SampleSummaryService {

  private static final Logger log = LoggerFactory.getLogger(SampleSummaryService.class);

  @Autowired private SampleLinkRepository sampleLinkRepository;

  @Autowired private CollectionExerciseRepository collectRepo;

  @Autowired private SampleSvcClient sampleSvcClient;

  @Autowired private CollectionExerciseService collexService;

  @Autowired private EventRepository eventRepository;

  public boolean enrichSample(UUID collectionExerciseId) {

    CollectionExercise collectionExercise = collectRepo.findOneById(collectionExerciseId);
    UUID surveyId = collectionExercise.getSurveyId();

    List<SampleLink> sampleLinks =
        sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId);
    List<UUID> sampleSummaryIdList =
        sampleLinks.stream().map(SampleLink::getSampleSummaryId).collect(Collectors.toList());

    if (sampleSummaryIdList.size() > 1) {
      log.with("numberOfSampleSummaries", sampleSummaryIdList.size())
          .with("collectionExerciseId", collectionExerciseId)
          .error("Multiple sample summaries detected during enrichment phase");
    }
    // in rasrm business there can only ever be one sample summary per collection exercise
    UUID sampleSummaryId = sampleSummaryIdList.get(0);
    boolean successfulEnrichment =
        sampleSvcClient.enrichSampleSummary(surveyId, collectionExerciseId, sampleSummaryId);

    // TODO change this to be pubsub
    validSample(successfulEnrichment, collectionExerciseId);

    log.with("successfulEnrichment", successfulEnrichment)
        .with("sampleSummaryId", sampleSummaryId)
        .info("Enrichment complete");
    return successfulEnrichment;
  }

  public boolean distributeSample(UUID collectionExerciseId) {
    List<SampleLink> sampleLinks =
        sampleLinkRepository.findByCollectionExerciseId(collectionExerciseId);
    List<UUID> sampleSummaryIdList =
        sampleLinks.stream().map(SampleLink::getSampleSummaryId).collect(Collectors.toList());

    if (sampleSummaryIdList.size() > 1) {
      log.with("numberOfSampleSummaries", sampleSummaryIdList.size())
          .with("collectionExerciseId", collectionExerciseId)
          .error("Multiple sample summaries detected during distribution phase");
    }
    // in rasrm business there can only ever be one sample summary per collection exercise
    UUID sampleSummaryId = sampleSummaryIdList.get(0);
    boolean successfulDistribution = sampleSvcClient.distributeSampleSummary(sampleSummaryId);
    // TODO change this to be pubsub
    sampleSummaryDistributed(successfulDistribution, collectionExerciseId);
    log.with("successfulDistribution", successfulDistribution)
        .with("sampleSummaryId", sampleSummaryId)
        .info("Distribution complete");
    return successfulDistribution;
  }

  /**
   * Transitions the state of a collection exercise depending on the outcome of the validation of
   * the sample summary
   *
   * @param valid true if sample summary valid and enriched, false otherwise
   * @param collectionExerciseId the id of the collection exercise
   */
  public void validSample(boolean valid, UUID collectionExerciseId) {
    CollectionExercise collectionExercise = collectRepo.findOneById(collectionExerciseId);
    CollectionExerciseDTO.CollectionExerciseEvent event;
    if (valid) {
      log.with("collectionExerciseId", collectionExerciseId).info("collection exercise valid");
      event = CollectionExerciseDTO.CollectionExerciseEvent.VALIDATE;
    } else {
      log.with("collectionExerciseId", collectionExerciseId).info("collection exercise invalid");
      event = CollectionExerciseDTO.CollectionExerciseEvent.INVALIDATE;
    }
    try {
      log.with("collectionExerciseId", collectionExerciseId).info("transitioning state");
      collexService.transitionCollectionExercise(collectionExercise, event);
    } catch (CTPException e) {
      log.error("unable to transition collection exercise", e);
    }
    if (valid) {
      log.info("request distribution of sample");
      distributeSample(collectionExerciseId);
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
  public void sampleSummaryDistributed(boolean distributed, UUID collectionExerciseId) {
    CollectionExercise collectionExercise = collectRepo.findOneById(collectionExerciseId);
    CollectionExerciseDTO.CollectionExerciseEvent event;
    if (distributed) {
      log.with("collectionExerciseId", collectionExerciseId).info("collection exercise distibuted");
      // Check if go_live date is in the past
      if ((eventRepository
              .findOneByCollectionExerciseAndTag(
                  collectionExercise, EventService.Tag.go_live.name())
              .getTimestamp()
              .getTime())
          < System.currentTimeMillis()) {
        log.with("collectionExerciseId", collectionExerciseId)
            .debug("Attempting to transition collection exercise to Live");
        // All sample units published and go live date in past, set exercise state to LIVE
        event = CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE;
      } else {
        // All sample units published, set exercise state to READY_FOR_LIVE
        log.with("collection_exercise_id", collectionExerciseId)
            .debug("Attempting to transition collection exercise to Ready for Live");
        event = CollectionExerciseDTO.CollectionExerciseEvent.PUBLISH;

        // set the publish date time and save to the database
        collectionExercise.setActualPublishDateTime(new Timestamp(new Date().getTime()));
        collectRepo.saveAndFlush(collectionExercise);
      }
      try {
        log.with("collectionExerciseId", collectionExerciseId).info("transitioning state");
        collexService.transitionCollectionExercise(collectionExercise, event);
      } catch (CTPException e) {
        log.error("unable to transition collection exercise", e);
      }
    } else {
      log.with("collectionExerciseId", collectionExerciseId)
          .warn("collection exercise failed distibution");
    }
  }
}
