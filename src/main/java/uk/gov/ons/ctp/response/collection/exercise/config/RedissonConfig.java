package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Config POJO for Redisson */
@CoverageIgnore
@Data
public class RedissonConfig {
  private String address;
  private Integer listTimeToLiveSeconds;
  private Integer listTimeToWaitSeconds;
}
