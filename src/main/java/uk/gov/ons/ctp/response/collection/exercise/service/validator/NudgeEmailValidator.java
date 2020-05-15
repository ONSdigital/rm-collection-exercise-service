package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@Component
public class NudgeEmailValidator implements EventValidator {
  private final EventDateOrderChecker eventDateOrderChecker;

  public NudgeEmailValidator(EventDateOrderChecker eventDateOrderChecker) {
    this.eventDateOrderChecker = eventDateOrderChecker;
  }

  public void validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseDTO.CollectionExerciseState collectionExerciseState)
      throws CTPException {
    if (!isNudgeEmail(submittedEvent)) {
      return;
    }
    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));
    final Event goLive = existingEventsMap.get(Tag.go_live.toString());
    final Event returnBy = existingEventsMap.get(Tag.return_by.toString());
    if (isCollectionExerciseLockedState(collectionExerciseState)
        && isExistingReminderInPast(submittedEvent, existingEventsMap)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Nudge email cannot be set in the past");
    }
    if (!eventBetweenGoLiveAndReturnBy(goLive, submittedEvent, returnBy)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "Nudge email must be set after the Go Live date ("
              + goLive.getTimestamp()
              + ") "
              + "and before Return by date ("
              + returnBy.getTimestamp()
              + ")");
    }
    final List<Event> reminders =
        Tag.ORDERED_NUDGE_EMAIL
            .stream()
            .map(tag -> getEventByTag(tag, submittedEvent, existingEventsMap))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (!eventDateOrderChecker.isEventDatesInOrder(reminders)) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST, "Nudge Email must be set sequentially");
    }
  }

  private boolean isNudgeEmail(final Event event) {
    return Tag.valueOf(event.getTag()).isNudgeEmail();
  }

  private boolean eventBetweenGoLiveAndReturnBy(Event goLive, Event event, Event returnBy) {
    boolean isAfterGoLive = true;
    boolean isBeforeReturnEnd = true;

    if (event == null) {
      return true;
    }

    if (goLive != null) {
      isAfterGoLive = goLive.getTimestamp().before(event.getTimestamp());
    }

    if (returnBy != null) {
      isBeforeReturnEnd = event.getTimestamp().before(returnBy.getTimestamp());
    }

    return isAfterGoLive && isBeforeReturnEnd;
  }

  private boolean isCollectionExerciseLockedState(
      CollectionExerciseDTO.CollectionExerciseState collectionExerciseState) {
    final List<CollectionExerciseDTO.CollectionExerciseState> lockedStates =
        Arrays.asList(
            CollectionExerciseDTO.CollectionExerciseState.EXECUTION_STARTED,
            CollectionExerciseDTO.CollectionExerciseState.VALIDATED,
            CollectionExerciseDTO.CollectionExerciseState.EXECUTED,
            CollectionExerciseDTO.CollectionExerciseState.READY_FOR_LIVE,
            CollectionExerciseDTO.CollectionExerciseState.LIVE);

    return lockedStates.contains(collectionExerciseState);
  }

  private boolean isExistingReminderInPast(
      Event submittedEvent, Map<String, Event> existingEventsMap) {
    final Event existingReminder = existingEventsMap.get(submittedEvent.getTag());
    if (existingReminder == null) {
      return false;
    }
    final Timestamp currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    return existingReminder.getTimestamp().before(currentTimestamp);
  }

  private Event getEventByTag(Tag tag, Event submittedEvent, Map<String, Event> existingEvents) {
    return submittedEvent.getTag().equals(tag.toString())
        ? submittedEvent
        : existingEvents.get(tag.toString());
  }
}
