package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
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
    if (!isEventBetweenGoLiveAndReturnBy(goLive, submittedEvent, returnBy)) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
      sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
      Date goLiveDate = new Date(goLive.getTimestamp().getTime());
      Date returnByDate = new Date(returnBy.getTimestamp().getTime());
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "Nudge email must be set after the Go Live date ("
              + sdf.format(goLiveDate)
              + ") "
              + "and before Return by date ("
              + sdf.format(returnByDate)
              + ")");
    }
    List<Event> existingEvent =
        EventService.Tag.ORDERED_NUDGE_EMAIL
            .stream()
            .map(tag -> getExistingNudgeEmails(tag, existingEventsMap))
            .filter(Objects::nonNull)
            .filter(event -> isEventSameAsExisting(event, submittedEvent))
            .collect(Collectors.toList());
    if (existingEvent.size() > 0) {
      throw new CTPException(
          CTPException.Fault.BAD_REQUEST,
          "A nudge email has already been scheduled for this date and time. Choose a different date or time.");
    }
  }

  private boolean isNudgeEmail(final Event event) {
    return Tag.valueOf(event.getTag()).isNudgeEmail();
  }

  private boolean isEventBetweenGoLiveAndReturnBy(Event goLive, Event event, Event returnBy) {
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

  private Event getExistingNudgeEmails(EventService.Tag tag, Map<String, Event> existingEvents) {
    return existingEvents.get(tag.toString());
  }

  private boolean isEventSameAsExisting(Event nudgeEvent, Event submittedEvent) {
    return !(nudgeEvent.equals(submittedEvent))
        && nudgeEvent.getTimestamp().equals(submittedEvent.getTimestamp());
  }
}
