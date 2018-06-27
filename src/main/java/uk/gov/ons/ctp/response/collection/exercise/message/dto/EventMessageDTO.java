package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import lombok.Data;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

@Data
public class EventMessageDTO {
  private EventDTO event;

  private CollectionExerciseEventPublisher.MessageType messageType;

  public EventMessageDTO() {}

  public EventMessageDTO(CollectionExerciseEventPublisher.MessageType messageType, EventDTO event) {
    this.event = event;
    this.messageType = messageType;
  }
}
