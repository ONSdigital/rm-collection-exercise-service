package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;

/**
 * Config POJO for Swagger UI Generation
 */
@Data
public class SwaggerSettings {

  private Boolean swaggerUiActive;
  private String groupName;
  private String title;
  private String description;
  private String version;

}
