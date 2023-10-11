package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.SampleSummaryReadinessDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.SampleLinkRepository;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

@RestController
@RequestMapping(value = "/sample", produces = "application/json")
public class SampleSvcEndpoint {
  private static final Logger log = LoggerFactory.getLogger(SampleSvcEndpoint.class);
  private CollectionExerciseService collectionExerciseService;
  private final SampleLinkRepository sampleLinkRepository;

  @Autowired
  public SampleSvcEndpoint(
      CollectionExerciseService collectionExerciseService,
      SampleLinkRepository sampleLinkRepository) {
    this.collectionExerciseService = collectionExerciseService;
    this.sampleLinkRepository = sampleLinkRepository;
  }

  @PostMapping(value = "/summary-readiness")
  public ResponseEntity<Void> sampleSummaryReadiness(
      final @RequestBody @Valid SampleSummaryReadinessDTO sampleSummaryReadinessDTO)
      throws CTPException {
    log.with(sampleSummaryReadinessDTO.getSampleSummaryId()).info("Sample summary status updated");

    UUID sampleSummaryId = sampleSummaryReadinessDTO.getSampleSummaryId();
    UUID collectionExerciseId =
        sampleLinkRepository
            .findBySampleSummaryId(sampleSummaryId)
            .get(0)
            .getCollectionExerciseId();

    if (collectionExerciseId == null) {
      throw new CTPException(
          CTPException.Fault.RESOURCE_NOT_FOUND,
          String.format(
              "No collection exercise id linked with sample summary %s", sampleSummaryId));
    }

    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
        collectionExerciseId);
    return ResponseEntity.ok().build();
  }
}
