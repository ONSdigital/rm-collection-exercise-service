package uk.gov.ons.ctp.response.collection.exercise.service;

import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;

@FunctionalInterface
public interface EventChangeHandler {
    void handleEventLifecycle(CollectionExerciseEventPublisher.MessageType change, Event event) throws CTPException;
}
