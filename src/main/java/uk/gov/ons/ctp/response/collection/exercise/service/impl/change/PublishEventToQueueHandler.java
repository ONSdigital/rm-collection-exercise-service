package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

/**
 * EventChangeHandler to publish a message to a rabbit queue when the event changes
 */
@Component
@Slf4j
public final class PublishEventToQueueHandler implements EventChangeHandler {
    @Autowired
    private CollectionExerciseEventPublisher eventPublisher;

    @Override
    public void handleEventLifecycle(final CollectionExerciseEventPublisher.MessageType change, final Event event)
            throws CTPException {
        log.debug("Publishing {} message for {}", change, event);
        eventPublisher.publishCollectionExerciseEvent(change, EventService.createEventDTOFromEvent(event));
    }
}