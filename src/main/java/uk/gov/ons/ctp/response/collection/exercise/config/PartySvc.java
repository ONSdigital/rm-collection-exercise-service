package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;

/**
 * App config POJO for Party service access - host/location and endpoint
 * locations
 *
 */
@CoverageIgnore
@Data
public class PartySvc {
  private RestUtilityConfig connectionConfig;
  private String requestPartyPath;
}
