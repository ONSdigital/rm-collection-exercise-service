package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@Component
public class MandatoryEventValidator implements EventValidator {
  private final EventDateOrderChecker eventDateOrderChecker;

  public MandatoryEventValidator(EventDateOrderChecker eventDateOrderChecker) {
    this.eventDateOrderChecker = eventDateOrderChecker;
  }

  public void validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseState collectionExerciseState)
      throws CTPException {
    if (!isMandatory(submittedEvent)) {
      return;
    }

    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));

    final List<Event> mandatoryEvents =
        EventService.Tag.ORDERED_MANDATORY_EVENTS
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEventsMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (!eventDateOrderChecker.isEventDatesInOrder(mandatoryEvents)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Collection exercise events must be set sequentially");
    }
  }

  private Event getEventByTag(
      EventService.Tag tag, Event submittedEvent, Map<String, Event> existingEvents) {
    return submittedEvent.getTag().equals(tag.toString())
        ? submittedEvent
        : existingEvents.get(tag.toString());
  }

  private boolean isMandatory(final Event updatedEvent) {
    return EventService.Tag.valueOf(updatedEvent.getTag()).isMandatory();
  }
}
