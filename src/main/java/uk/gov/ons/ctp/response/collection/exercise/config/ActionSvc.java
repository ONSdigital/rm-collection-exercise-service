package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtilityConfig;

/** Application Config bean for the connection details to the Action Service */
@Data
@CoverageIgnore
public class ActionSvc {
  private RestUtilityConfig connectionConfig;
  private String actionPlansPath;
  private String actionRulesPath;
  private String actionRulePath;
  private String actionRulesForActionPlanPath;
  private String actionPlanPath;
  private String processEventPath;
  private boolean deprecated;
}
