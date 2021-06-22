package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import javax.validation.Valid;
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
    log.with(collectionInstrumentMessageDTO.getInstrumentId())
        .with(collectionInstrumentMessageDTO.getExerciseId())
        .info("Consumed message");

    UUID collectionExerciseId = collectionInstrumentMessageDTO.getExerciseId();

    collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(
        collectionExerciseId);
    return ResponseEntity.ok().build();
  }
}
