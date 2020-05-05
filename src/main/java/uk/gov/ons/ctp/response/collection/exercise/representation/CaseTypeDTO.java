package uk.gov.ons.ctp.response.collection.exercise.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/** CaseType API representation. */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CaseTypeDTO {

  @JsonProperty("sampleUnitType")
  private String sampleUnitTypeFK;

  private UUID actionPlanId;
}
