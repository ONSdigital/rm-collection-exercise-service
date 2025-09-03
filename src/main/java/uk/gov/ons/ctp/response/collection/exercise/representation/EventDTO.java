package uk.gov.ons.ctp.response.collection.exercise.representation;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class EventDTO {
  private UUID id;

  private UUID collectionExerciseId;

  @NotNull
  @Size(max = 20, min = 1)
  private String tag;

  @NotNull private Date timestamp;

  @Nullable private Status eventStatus;

  @Nullable
  public enum Status {
    SCHEDULED,
    PROCESSED,
    FAILED,
    RETRY,
    PROCESSING
  }
}
