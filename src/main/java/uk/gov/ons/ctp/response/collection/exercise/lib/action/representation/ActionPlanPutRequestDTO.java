package uk.gov.ons.ctp.response.collection.exercise.lib.action.representation;

import java.util.Date;
import java.util.HashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Domain model object for representation. */
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class ActionPlanPutRequestDTO {

  private String description;

  private Date lastRunDateTime;

  private HashMap<String, String> selectors;

  private String name;
}
