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
        String tagString = event.getTag();
        EventService.Tag tag;

        try {
            tag = EventService.Tag.valueOf(tagString);

            // If it's a go_live EventElapsed then attempt to transition the state of the collection exercise to live
            if (tag == EventService.Tag.go_live && messageType == CollectionExerciseEventPublisher.MessageType.EventElapsed){
                UUID collexId = event.getCollectionExerciseId();

                transitionCollectionExerciseToLive(collexId);
            } else {
                logIgnoreMessage(message);
            }
        } catch (IllegalArgumentException e) {
            // This means that the event isn't one of the mandatory events that is represented in the enum - which is
            // fine.
            logIgnoreMessage(message);
        }
    }

    /**
     * Logs a message stating the event message is being ignored as it's not go_live/EventElapsed
     * @param message the event message being ignored
     */
    private void logIgnoreMessage(final EventMessageDTO message){
        EventDTO event = message.getEvent();

        log.info("Ignoring event message {}/{} - not {}/{}", event.getTag(), message.getMessageType(), EventService.Tag.go_live.name(), CollectionExerciseEventPublisher.MessageType.EventElapsed);
    }

    /**
     * Sends a GO_LIVE event to the state machine for a collection exercise
     * @param collexId the UUID of the collection exercise
     */
    private void transitionCollectionExerciseToLive(final UUID collexId){
        try {
            this.collectionExerciseService.transitionCollectionExercise(collexId,
                    CollectionExerciseDTO.CollectionExerciseEvent.GO_LIVE);
            log.info("Set collection exercise {} to LIVE state", collexId);
        } catch (CTPException e) {
            log.error("Failed to set collection exercise {} to LIVE state - {}", collexId, e);
        }
    }

}
