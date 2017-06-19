package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import uk.gov.ons.ctp.common.rest.RestClientConfig;

/**
 * App config POJO for CollectionInstrument service access - host/location and endpoint
 * locations
 *
 */
@Data
public class CollectionInstrumentSvc {
  private RestClientConfig connectionConfig;
  private String requestCollectionInstruments;

}
