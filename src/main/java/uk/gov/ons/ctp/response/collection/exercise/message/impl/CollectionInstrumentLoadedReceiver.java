package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.CollectionInstrumentMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;

@MessageEndpoint
@Slf4j
public class CollectionInstrumentLoadedReceiver {

  @Autowired private CollectionExerciseService collectionExerciseService;

  @ServiceActivator(inputChannel = "ciMessageDto")
  public void acceptSampleUnit(CollectionInstrumentMessageDTO message) throws CTPException {
    log.info("json: {}", message);

    UUID collexId = message.getExerciseId();

    this.collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(collexId);
  }
}
