package uk.gov.ons.ctp.response.collection.exercise.message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@Data
public class CollectionInstrumentMessageDTO {
  private String action;
  private UUID exerciseId;

  @JsonCreator
  public CollectionInstrumentMessageDTO(
      @JsonProperty("action") String action,
      @JsonProperty("exercise_id") String exerciseId{
    this.action = action;
    this.exerciseId = UUID.fromString(exerciseId);
  }
}
