package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class SampleSummaryStatusDTO {
  private UUID collectionExerciseId;

  private boolean successful;

  public enum Event {
    DISTRIBUTED,
    ENRICHED
  }

  private Event event;
}
