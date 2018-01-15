package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class CollectionExerciseEventPublisherImpl implements CollectionExerciseEventPublisher {

    @Autowired
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Qualifier("collexEventTemplate")
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void publishCollectionExerciseEvent(UUID eventUuid) throws CTPException {
        Event event = this.eventService.getEvent(eventUuid);

        if (event == null){
            throw new CTPException(CTPException.Fault.RESOURCE_NOT_FOUND, String.format("Event %s does not exist", eventUuid));
        } else {
            publishCollectionExerciseEvent(eventUuid, event.getCollectionExercise().getId(), event.getTag(), event.getTimestamp());
        }
    }

    @Override
    public void publishCollectionExerciseEvent(UUID eventUuid, UUID collectionExerciseUuid, String tag, Date timestamp) throws CTPException {
        EventDTO eventDto = new EventDTO();

        eventDto.setCollectionExerciseId(collectionExerciseUuid);
        eventDto.setId(eventUuid);
        eventDto.setTimestamp(timestamp);
        eventDto.setTag(tag);

        try {
            String message = this.objectMapper.writeValueAsString(eventDto);
            this.rabbitTemplate.convertAndSend(message);
            this.eventService.setEventMessageSent(eventUuid);
        } catch (CTPException e) {
            String message = String.format("Failed to set event %s as message sent", eventUuid);

            log.error(message);

            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
        } catch (JsonProcessingException e) {
            String message = String.format("Failed to serialise event %s to json", eventDto);

            log.error(message);

            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
        }
    }
}
