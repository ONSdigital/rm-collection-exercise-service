package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

@Component
public class ValidateExistingNudgeEmail {
  private final EventDateOrderChecker eventDateOrderChecker;

  public ValidateExistingNudgeEmail(EventDateOrderChecker eventDateOrderChecker) {
    this.eventDateOrderChecker = eventDateOrderChecker;
  }

  public List<Event> validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseDTO.CollectionExerciseState collectionExerciseState)
      throws CTPException {
    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));
    return EventService.Tag.ORDERED_NUDGE_EMAIL
        .stream()
        .map(tag -> getEventByTag(tag, existingEventsMap))
        .filter(Objects::nonNull)
        .filter(event -> isEventAfterReturnBy(event, submittedEvent))
        .collect(Collectors.toList());
  }

  private boolean isEventAfterReturnBy(Event nudgeEvent, Event submittedEvent) {
    return nudgeEvent.getTimestamp().after(submittedEvent.getTimestamp());
  }

  private Event getEventByTag(EventService.Tag tag, Map<String, Event> existingEvents) {
    System.out.println("Getting there :::" + existingEvents.get(tag.toString()));
    ;
    return existingEvents.get(tag.toString());
  }
}
