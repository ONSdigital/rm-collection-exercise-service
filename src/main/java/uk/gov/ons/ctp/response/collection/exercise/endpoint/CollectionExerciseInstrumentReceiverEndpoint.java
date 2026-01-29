package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import static net.logstash.logback.argument.StructuredArguments.kv;

import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

@RestController
@RequestMapping(value = "/collection-instrument", produces = "application/json")
public class CollectionExerciseInstrumentReceiverEndpoint {
  private static final Logger log =
      LoggerFactory.getLogger(CollectionExerciseInstrumentReceiverEndpoint.class);
  private CollectionExerciseService collectionExerciseService;

  @Autowired
  public CollectionExerciseInstrumentReceiverEndpoint(
      CollectionExerciseService collectionExerciseService) {
    this.collectionExerciseService = collectionExerciseService;
  }

  @RequestMapping(value = "/link", method = RequestMethod.POST)
  public ResponseEntity<?> collectionInstrumentLink(
      final @RequestBody @Valid CollectionInstrumentMessageDTO collectionInstrumentMessageDTO)
      throws CTPException {
    log.info(
        "Collection instruments updated",
        kv("collectionExerciseId", collectionInstrumentMessageDTO.getExerciseId()));

    UUID collectionExerciseId = collectionInstrumentMessageDTO.getExerciseId();

    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
        collectionExerciseId);
    return ResponseEntity.ok().build();
  }
}
