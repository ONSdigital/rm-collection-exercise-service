package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation;

import java.time.OffsetDateTime;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionRulePutRequestDTO {

  @Size(max = 100)
  private String name;

  @Size(max = 250)
  private String description;

  private OffsetDateTime triggerDateTime;

  @Min(value = 1)
  @Max(value = 5)
  private Integer priority;
}
