package uk.gov.ons.ctp.response.collection.exercise.service.impl.change;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.service.CollectionExerciseService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventChangeHandler;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
@Slf4j
public class ScheduledStartDateHandler implements EventChangeHandler {

    @Autowired
    private CollectionExerciseService collectionExerciseService;

    @Override
    public void handleEventLifecycle(CollectionExerciseEventPublisher.MessageType change, Event event) {
        switch(change){
            case EventCreated:
            case EventUpdated:
                updateCollectionExerciseFromEvent(event);
                break;
            default:
                break;
        }
    }

    private void updateCollectionExerciseFromEvent(Event event){
        try {
            EventService.Tag tag = EventService.Tag.valueOf(event.getTag());

            switch(tag){
                case mps:
                    CollectionExercise collex = event.getCollectionExercise();

                    if (collex != null) {
                        log.info("Setting scheduledStartDate for {} to {}", collex.getId(), event.getTimestamp());
                        collex.setScheduledStartDateTime(event.getTimestamp());
                        this.collectionExerciseService.updateCollectionExercise(collex);
                    }
                    break;
                default:
                    break;
            }
        } catch(IllegalArgumentException e){
            // Thrown if tag isn't one of the mandatory types - if it happens we don't care about the event
        }
    }
}
