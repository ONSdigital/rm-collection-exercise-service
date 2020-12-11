package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Process event payload JSON representation. */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ProcessEventDTO {

  private String tag;
  private UUID collectionExerciseId;
}
