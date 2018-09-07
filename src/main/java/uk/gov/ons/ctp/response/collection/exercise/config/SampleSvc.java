package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import uk.gov.ons.ctp.common.rest.RestUtilityConfig;

/** App config POJO for Sample service access - host/location and endpoint locations */
@CoverageIgnore
@Data
public class SampleSvc {
  private RestUtilityConfig connectionConfig;
  private String requestSampleUnitsPath;
  private String requestSampleUnitSizePath;
}
