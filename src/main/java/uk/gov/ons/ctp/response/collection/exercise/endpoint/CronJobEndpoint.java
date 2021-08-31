package uk.gov.ons.ctp.response.collection.exercise.endpoint;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.ctp.response.collection.exercise.domain.CollectionExercise;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.repository.CollectionExerciseRepository;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;

/** The REST endpoint controller for ActionDistributor. */
@RestController
@RequestMapping(value = "/cron", produces = "application/json")
public class CronJobEndpoint {
  private static final Logger log = LoggerFactory.getLogger(CronJobEndpoint.class);

  private final CollectionExerciseRepository collectRepo;
  private final SampleService sampleService;
  private final EventService eventService;

  @Autowired
  public CronJobEndpoint(
      CollectionExerciseRepository collectRepo,
      SampleService sampleService,
      EventService eventService) {
    this.collectRepo = collectRepo;
    this.sampleService = sampleService;
    this.eventService = eventService;
  }

  /**
   * Finds all the validated collection exercises and distributes them.
   *
   * <p>Distributing a sample unit means sending a message to the case service with details about
   * the sample unit and transitioning the state of the sample in collection-exercise to mark the
   * event happening.
   *
   * @throws CTPException on any exception thrown
   */
  @RequestMapping(value = "/sample-unit-distribution", method = RequestMethod.GET)
  public final ResponseEntity<String> distributeSampleUnits() throws CTPException {
    try {
      log.info("About to begin sample unit distribution");
      List<CollectionExercise> exercises =
          collectRepo.findByState(CollectionExerciseDTO.CollectionExerciseState.VALIDATED);

      log.info("Found [" + exercises.size() + "] collection exercises to distribute");
      for (CollectionExercise collectionExercise : exercises) {
        sampleService.distributeSampleUnits(collectionExercise);
      }
      log.info("Completed sample unit distribution");
      return ResponseEntity.ok().body("Completed sample unit distribution");
    } catch (RuntimeException e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when validating sample units");
    }
  }

  /**
   * Gets all the SCHEDULED events for active collection exercises and if an event requires an
   * action to be taken (i.e., a letter or email to be sent) sends it to action to be acted on
   *
   * @throws CTPException on any exception thrown
   */
  @RequestMapping(value = "/process-scheduled-events", method = RequestMethod.GET)
  public final ResponseEntity<String> processScheduledEvents() throws CTPException {
    try {
      log.info("About to begin processing scheduled events");
      eventService.processEvents();
      log.info("Completed processing scheduled events");
      return ResponseEntity.ok().body("Completed processing scheduled events");
    } catch (RuntimeException e) {
      log.error(
          "Uncaught exception - transaction rolled back. Will re-run when scheduled by cron", e);
      throw new CTPException(
          CTPException.Fault.SYSTEM_ERROR, "Uncaught exception when processing scheduled events");
    }
  }
}
