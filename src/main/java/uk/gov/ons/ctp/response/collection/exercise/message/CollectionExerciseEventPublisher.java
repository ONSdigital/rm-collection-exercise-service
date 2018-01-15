package uk.gov.ons.ctp.response.collection.exercise.message;

import uk.gov.ons.ctp.common.error.CTPException;

import java.util.Date;
import java.util.UUID;

public interface CollectionExerciseEventPublisher {
    void publishCollectionExerciseEvent(UUID eventUuid) throws CTPException;
    void publishCollectionExerciseEvent(UUID eventUuid, UUID collectionExerciseUuid, String tag, Date timestamp) throws CTPException ;
}
