package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseActionEventStatusDTO {
  public enum EventTag {
    mps,
    go_live,
    reminder,
    reminder2,
    reminder3,
    nudge_email_0,
    nudge_email_1,
    nudge_email_2,
    nudge_email_3,
    nudge_email_4
  }

  @NotNull private UUID collectionExerciseID;
  @NotNull private EventTag tag;
  @NotNull private EventDTO.Status status;
}
