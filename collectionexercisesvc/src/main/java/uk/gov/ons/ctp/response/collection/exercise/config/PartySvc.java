package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import uk.gov.ons.ctp.common.rest.RestClientConfig;

/**
 * App config POJO for Party service access - host/location and endpoint
 * locations
 *
 */
@Data
public class PartySvc {
  private RestClientConfig connectionConfig;
  private String requestPartyPath;
}
