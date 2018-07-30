package uk.gov.ons.ctp.response.collection.exercise.service;

import java.util.List;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;

public interface EventValidator {

  boolean validate(
      final List<Event> existingEvents,
      final Event updatedEvent,
      final CollectionExerciseDTO.CollectionExerciseState collectionExerciseState);

  boolean validateOnCreate(
      final List<Event> existingEvents,
      final Event newEvent,
      final CollectionExerciseDTO.CollectionExerciseState collectionExerciseState);
}
