package uk.gov.ons.ctp.response.collection.exercise;

import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.stereotype.Component;

/**
 * Orika Mapper facade is not supported in Java 17. Mapper facade has been replaced by
 * ObjectConverter
 */
@CoverageIgnore
@Component
public class CollectionExerciseMessageType {

  public enum MessageType {
    EventElapsed,
    EventCreated,
    EventUpdated,
    EventDeleted
  }
}
