package uk.gov.ons.ctp.response.collection.exercise.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * CaseType API representation.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class CaseTypeDTO {

  @JsonProperty("sampleUnitType")
  String sampleUnitTypeFK;

  UUID actionPlanId;

}
