package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.EventMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
public class CollectionExerciseEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEventPublisher.class);

  public enum MessageType {
    EventElapsed,
    EventCreated,
    EventUpdated,
    EventDeleted
  }

  @Autowired
  @Qualifier("customObjectMapper")
  private ObjectMapper objectMapper;

  @Autowired
  @Qualifier("collexEventTemplate")
  private RabbitTemplate rabbitTemplate;

  @Autowired private EventService eventService;

  public void publishCollectionExerciseEvent(MessageType messageType, EventDTO eventDto)
      throws CTPException {
    EventMessageDTO messageDto = new EventMessageDTO(messageType, eventDto);

    try {
      String message = objectMapper.writeValueAsString(messageDto);
      rabbitTemplate.convertAndSend(message);
      if (messageType == MessageType.EventElapsed) {
        eventService.setEventMessageSent(eventDto.getId());
      }
    } catch (CTPException e) {
      String message = String.format("Failed to set event %s as message sent", eventDto.getId());

      log.with("event_id", eventDto.getId()).error(message, e);

      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
    } catch (JsonProcessingException e) {
      String message = String.format("Failed to serialise event %s to json", eventDto);

      log.with("event", eventDto).error(message, e);

      throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
    }
  }
}
