package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

@MessageEndpoint
public class CollectionInstrumentLoadedReceiver {
  private static final Logger log =
      LoggerFactory.getLogger(CollectionInstrumentLoadedReceiver.class);

  @Autowired private CollectionExerciseService collectionExerciseService;

  @ServiceActivator(inputChannel = "ciMessageDto")
  public void acceptSampleUnit(CollectionInstrumentMessageDTO message) throws CTPException {
    log.info("json: {}", message);

    UUID collexId = message.getExerciseId();

    this.collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(collexId);
  }
}
