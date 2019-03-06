package uk.gov.ons.ctp.response.collection.exercise.validation;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/**
 * Schedule Validation of sample units in INIT state checking Collection Instruments exist for each
 * sample unit.
 */
@Component
public class ValidationScheduler {

  private SampleService sampleService;
  private static final Logger log = LoggerFactory.getLogger(ValidationScheduler.class);

  @Autowired public AppConfig appConfig;

  @Autowired
  public ValidationScheduler(SampleService sampleService) {
    this.sampleService = sampleService;
  }

  /** Carry out scheduled validation according to configured fixed delay. */
  @Scheduled(fixedDelayString = "#{appConfig.schedules.validationScheduleDelayMilliSeconds}")
  public void scheduleValidation() {
    sampleService.validateSampleUnits();
  }
}
