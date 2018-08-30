package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.error.CTPException.Fault;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.EventMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.repository.EventRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

@Component
public class CollectionExerciseEventPublisher {
  private static final Logger log = LoggerFactory.getLogger(CollectionExerciseEventPublisher.class);

  public enum MessageType {
    EventElapsed,
    EventCreated,
    EventUpdated,
    EventDeleted
  }

  private ObjectMapper objectMapper;
  private RabbitTemplate rabbitTemplate;
  private EventRepository eventRepository;

  public CollectionExerciseEventPublisher(
      EventRepository eventRepository,
      @Qualifier("customObjectMapper") ObjectMapper objectMapper,
      @Qualifier("collexEventTemplate") RabbitTemplate rabbitTemplate) {
    this.eventRepository = eventRepository;
    this.objectMapper = objectMapper;
    this.rabbitTemplate = rabbitTemplate;
  }

  public void publishCollectionExerciseEvent(MessageType messageType, EventDTO eventDto)
      throws CTPException {
    EventMessageDTO messageDto = new EventMessageDTO(messageType, eventDto);

    try {
      String message = this.objectMapper.writeValueAsString(messageDto);
      this.rabbitTemplate.convertAndSend(message);
      if (messageType == MessageType.EventElapsed) {
        setEventMessageSent(eventDto.getId());
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

  private void setEventMessageSent(UUID eventId) throws CTPException {
    Event event = getEvent(eventId);

    event.setMessageSent(new Timestamp(new Date().getTime()));

    this.eventRepository.save(event);
  }

  private Event getEvent(UUID eventId) throws CTPException {
    Event event = this.eventRepository.findOneById(eventId);

    if (event == null) {
      throw new CTPException(
          Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", event));
    } else {
      return event;
    }
  }
}
