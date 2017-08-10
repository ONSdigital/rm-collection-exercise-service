package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/**
 * Config POJO for Swagger UI Generation
 */
@CoverageIgnore
@Data
public class SwaggerSettings {

  private Boolean swaggerUiActive;
  private String groupName;
  private String title;
  private String description;
  private String version;

}
