package uk.gov.ons.ctp.response.collection.exercise.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/**
 * Schedule Validation of sample units in INIT state checking Collection Instruments exist for
 * each sample unit.
 *
 */
@Component
public class ValidationScheduler {

  @Autowired
  private SampleService sampleService;

  /**
   * Carry out scheduled validation according to configured fixed delay.
   */
  @Scheduled(fixedDelayString = "#{appConfig.schedules.validationScheduleDelayMilliSeconds}")
  public void scheduleValidation() {
    sampleService.validateSampleUnits();
  }
}
