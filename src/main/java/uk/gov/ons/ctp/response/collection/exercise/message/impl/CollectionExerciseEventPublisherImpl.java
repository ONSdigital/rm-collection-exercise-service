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
import uk.gov.ons.ctp.response.collection.exercise.message.dto.EventMessageDTO;
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
    public void publishCollectionExerciseEvent(MessageType messageType, EventDTO eventDto) throws CTPException {
        EventMessageDTO messageDto = new EventMessageDTO(messageType, eventDto);

        try {
            String message = this.objectMapper.writeValueAsString(messageDto);
            this.rabbitTemplate.convertAndSend(message);
            if (messageType == MessageType.EventElapsed) {
                this.eventService.setEventMessageSent(eventDto.getId());
            }
        } catch (CTPException e) {
            String message = String.format("Failed to set event %s as message sent", eventDto.getId());

            log.error(message);

            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
        } catch (JsonProcessingException e) {
            String message = String.format("Failed to serialise event %s to json", eventDto);

            log.error(message);

            throw new CTPException(CTPException.Fault.SYSTEM_ERROR, message);
        }
    }

}
