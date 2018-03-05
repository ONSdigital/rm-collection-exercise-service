package uk.gov.ons.ctp.response.collection.exercise.message.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.EventMessageDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.util.UUID;

/**
 * Class to hold service activator method to handle incoming collection exercise event lifecycle messages
 */
@MessageEndpoint
@Slf4j
public class CollectionExerciseEventInboundReceiver {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    /**
     * Service activator method - if it's a go_live event and it's EventElapsed then send a GO_LIVE event to the
     * collection exercise to push it into the LIVE state
     *
     * @param message an event message
     */
    @ServiceActivator(inputChannel = "cemInMessage")
    public void handleEventMessage(final EventMessageDTO message) {
        EventDTO event = message.getEvent();
        CollectionExerciseEventPublisher.MessageType messageType = message.getMessageType();
        EventService.Tag tag = EventService.Tag.valueOf(event.getTag());

        // If it's a go_live event then attempt to transition the state of the collection exercise to live
        if (tag == EventService.Tag.go_live
                && messageType == CollectionExerciseEventPublisher.MessageType.EventElapsed) {
            UUID collexId = event.getCollectionExerciseId();

            try {
                this.collectionExerciseService.transitionCollectionExercise(collexId,
                        CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
            } catch (CTPException e) {
                log.error("Failed to set collection exerise to live state - {}", e);
            }
        }
    }
}
