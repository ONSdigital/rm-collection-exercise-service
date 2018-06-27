package uk.gov.ons.ctp.response.collection.exercise.message;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

public interface CollectionExerciseEventPublisher {

  enum MessageType {
    EventElapsed,
    EventCreated,
    EventUpdated,
    EventDeleted
  }

  void publishCollectionExerciseEvent(
      CollectionExerciseEventPublisher.MessageType messageType, EventDTO messageDto)
      throws CTPException;
}
