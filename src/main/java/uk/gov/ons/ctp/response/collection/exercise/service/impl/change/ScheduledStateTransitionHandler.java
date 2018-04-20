package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

/**
 * This is an EventChangeHandler that will fire the relevant EVENTS_ADDED/EVENTS_DELETED event at the corresponding
 * collection exercise when an event is updated.  In the event it's EVENTS_ADDED this will also give the collection
 * exercise a kick to check whether it should transition to READY_FOR_LIVE
 */
@Component
@Slf4j
public class ScheduledStateTransitionHandler implements EventChangeHandler {

    @Autowired
    private EventService eventService;

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Override
    public void handleEventLifecycle(CollectionExerciseEventPublisher.MessageType change, Event event) throws CTPException {
        CollectionExercise collectionExercise = event.getCollectionExercise();
        CollectionExerciseDTO.CollectionExerciseEvent ceEvent = null;

        try {
            if (this.eventService.isScheduled(collectionExercise.getId())) {
                ceEvent = CollectionExerciseDTO.CollectionExerciseEvent.EVENTS_ADDED;
                this.collectionExerciseService.transitionCollectionExercise(collectionExercise, ceEvent);
                // This is a bit of a kludge on account of SCHEDULED not being a proper state.  If the CI & sample are
                // already loaded, loading the events may trigger a transition straight through to READY_FOR_REVIEW
                // Performing this call here will force that
                this.collectionExerciseService.transitionScheduleCollectionExerciseToReadyToReview(collectionExercise);
            } else {
                ceEvent = CollectionExerciseDTO.CollectionExerciseEvent.EVENTS_DELETED;
                this.collectionExerciseService.transitionCollectionExercise(collectionExercise, ceEvent);
            }
        } catch(CTPException e) {
            // As the events are deliberately fired indiscriminately (i.e. the collection exercise state is not
            // checked first) there is a reasonable likelihood that the transition will fail harmlessly. Hence this
            // exception is being logged as a warning minus the stack trace
            log.warn("Collection exercise {} failed to handle state transition {} - {} ({})",
                    collectionExercise.getId(), ceEvent, e.getMessage(), e.getFault());
        }
    }
}
