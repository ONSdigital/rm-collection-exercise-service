package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPlanDTO {

  @NotNull private UUID id;

  private String name;

  private String description;

  private String createdBy;

  private Date lastRunDateTime;

  private HashMap<String, String> selectors;
}
