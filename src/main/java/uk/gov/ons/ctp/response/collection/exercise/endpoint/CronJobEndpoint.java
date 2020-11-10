package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** The REST endpoint controller for ActionDistributor. */
@RestController
@RequestMapping(value = "/cron", produces = "application/json")
public class CronJobEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CronJobEndpoint.class);

  private final SampleService sampleService;

  @Autowired
  public CronJobEndpoint(SampleService sampleService) {
    this.sampleService = sampleService;
  }

  /**
   * Calling this invokes the distribution of actions to either ras-rm-notify or the
   * ras-rm-print-file to create either emails or letters.
   *
   * @throws CTPException on any exception thrown
   */
  @RequestMapping(value = "/sample-unit-validation", method = RequestMethod.GET)
  public final ResponseEntity<String> validateSampleUnits() throws CTPException {
    try {
      log.info("About to begin sample unit validation");
      sampleService.validateSampleUnits();
      log.info("Completed sample unit validation");
      return ResponseEntity.ok().body("Completed sample unit validation");
    } catch (RuntimeException e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when validating sample units");
    }
  }
}
