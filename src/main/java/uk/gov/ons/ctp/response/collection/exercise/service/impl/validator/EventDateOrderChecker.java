package uk.gov.ons.ctp.response.collection.exercise.service.impl.validator;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;

@Component
public class EventDateOrderChecker {

  public boolean isEventDatesInOrder(List<Event> events) {
    Event[] eventsArray = events.stream().toArray(Event[]::new);
    boolean result = true;
    for (int i = 0; i < eventsArray.length - 1; i++) {
      Timestamp t1 = eventsArray[i].getTimestamp();
      Timestamp t2 = eventsArray[i + 1].getTimestamp();
      if (t1.after(t2)) {
        result = false;
      }
    }
    return result;
  }
}
