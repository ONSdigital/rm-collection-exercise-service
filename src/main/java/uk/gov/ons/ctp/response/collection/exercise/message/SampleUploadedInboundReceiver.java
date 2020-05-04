package uk.gov.ons.ctp.response.collection.exercise.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.response.collection.exercise.domain.SampleLink;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.lib.sample.representation.SampleSummaryDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** Class to hold service activator method to handle incoming sample uploaded messages */
@MessageEndpoint
public class SampleUploadedInboundReceiver {
  private static final Logger log = LoggerFactory.getLogger(SampleUploadedInboundReceiver.class);

  @Autowired private CollectionExerciseService collectionExerciseService;

  @Autowired private SampleService sampleService;

  /**
   * Attempt to transition collection exercise now sample has been uploaded
   *
   * @param sampleSummary the sample summary for which the upload has completed
   */
  @ServiceActivator(inputChannel = "sampleUploadedSampleSummaryInMessage")
  public void sampleUploaded(final SampleSummaryDTO sampleSummary) {
    List<SampleLink> links = this.sampleService.getSampleLinksForSummary(sampleSummary.getId());
    links
        .stream()
        .map(SampleLink::getCollectionExerciseId)
        .distinct()
        .forEach(this::transitionCollectionExercise);
  }

  private void transitionCollectionExercise(final UUID collectionExerciseId) {
    try {
      collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
          collectionExerciseId);
    } catch (CTPException e) {
      log.with("collection_exercise", collectionExerciseId)
          .error("Failed to transition collectionExerciseId", e);
    }
  }
}
