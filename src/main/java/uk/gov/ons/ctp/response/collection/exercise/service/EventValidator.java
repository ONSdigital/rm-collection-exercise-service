package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;

public interface EventValidator {

  void validate(
      final List<Event> existingEvents,
      final Event submittedEvent,
      final CollectionExerciseState collectionExerciseState)
      throws CTPException;
}
