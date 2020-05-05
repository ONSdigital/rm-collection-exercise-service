package uk.gov.ons.ctp.response.collection.exercise.service.validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService.Tag;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;

@Component
public class ReferencePeriodEventValidator implements EventValidator {

  public void validate(
      List<Event> existingEvents,
      Event submittedEvent,
      CollectionExerciseState collectionExerciseState)
      throws CTPException {
    if (!isReferencePeriod(submittedEvent)) {
      return;
    }

    final String tagName = submittedEvent.getTag();
    final String refPeriodStart = Tag.ref_period_start.toString();
    final String refPeriodEnd = Tag.ref_period_end.toString();

    final Map<String, Event> existingEventsMap =
        existingEvents.stream().collect(Collectors.toMap(Event::getTag, Function.identity()));

    final Event referencePeriodStart =
        (Tag.ref_period_start.hasName(tagName))
            ? submittedEvent
            : existingEventsMap.get(refPeriodStart);
    final Event referencePeriodEnd =
        (Tag.ref_period_end.hasName(tagName))
            ? submittedEvent
            : existingEventsMap.get(refPeriodEnd);

    if (referencePeriodStart != null && referencePeriodEnd != null) {
      if (referencePeriodStart.getTimestamp().after(referencePeriodEnd.getTimestamp())) {
        throw new CTPException(
            CTPException.Fault.BAD_REQUEST, "Reference period end date must be after start date");
      }
    }
  }

  private boolean isReferencePeriod(final Event event) {
    final List<String> referencePeriods =
        Arrays.asList(Tag.ref_period_start.toString(), Tag.ref_period_end.toString());
    return referencePeriods.contains(event.getTag());
  }
}
