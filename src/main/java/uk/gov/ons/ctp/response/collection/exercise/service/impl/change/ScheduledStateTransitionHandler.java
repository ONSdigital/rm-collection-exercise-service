package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

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

@Component
public class ScheduledStateTransitionHandler implements EventChangeHandler {

    @Autowired
    @Qualifier("collectionExercise")
    private StateTransitionManager<CollectionExerciseDTO.CollectionExerciseState, CollectionExerciseDTO.CollectionExerciseEvent> collectionExerciseTransitionState;

    @Autowired
    private EventService eventService;

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Override
    public void handleEventLifecycle(CollectionExerciseEventPublisher.MessageType change, Event event) throws CTPException {
        CollectionExercise collectionExercise = event.getCollectionExercise();

        if (this.eventService.isScheduled(collectionExercise.getId())){
            this.collectionExerciseService.transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EVENTS_ADDED);
        } else {
            this.collectionExerciseService.transitionCollectionExercise(collectionExercise, CollectionExerciseDTO.CollectionExerciseEvent.EVENTS_DELETED);
        }
    }
}
