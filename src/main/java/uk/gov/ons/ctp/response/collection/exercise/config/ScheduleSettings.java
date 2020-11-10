package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;

/** Config POJO for Scheduled threads */
@CoverageIgnore
@Data
public class ScheduleSettings {
  private String distributionScheduleDelayMilliSeconds;
  private Integer distributionScheduleRetrievalMax;
}
