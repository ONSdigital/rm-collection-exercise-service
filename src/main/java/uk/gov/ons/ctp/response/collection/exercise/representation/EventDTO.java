package uk.gov.ons.ctp.response.collection.exercise.representation;

import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

  @Nullable
  public enum Status {
    SCHEDULED,
    INPROGRESS,
    COMPLETED,
    FAILED,
    RETRY,
    PROCESSING
  }
}
