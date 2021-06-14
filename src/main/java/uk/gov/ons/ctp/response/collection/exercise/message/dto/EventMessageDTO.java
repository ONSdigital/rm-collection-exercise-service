package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import lombok.Data;
import uk.gov.ons.ctp.response.collection.exercise.CollectionExerciseBeanMapper.MessageType;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

@Data
public class EventMessageDTO {
  private EventDTO event;

  private MessageType messageType;

  public EventMessageDTO() {}

  public EventMessageDTO(MessageType messageType, EventDTO event) {
    this.event = event;
    this.messageType = messageType;
  }
}
