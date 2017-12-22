package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;

/**
 * App config POJO for Survey service access - host/location and endpoint
 * locations
 *
 */
@CoverageIgnore
@Data
public class SurveySvc {
  private RestUtilityConfig connectionConfig;
  private String requestClassifierTypesListPath;
  private String requestClassifierTypesPath;
  private String surveyDetailPath;
}
