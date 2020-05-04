package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionRuleDTO {

  private UUID id;
  private String name;
  private String description;
  private OffsetDateTime triggerDateTime;
  private Integer priority;
  private ActionType actionTypeName;
}
