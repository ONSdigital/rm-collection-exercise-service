package uk.gov.ons.ctp.response.collection.exercise.schedule;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.Date;
import java.util.UUID;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.message.CollectionExerciseEventPublisher;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;

@Component
public class EventJob implements Job {
  private static final Logger log = LoggerFactory.getLogger(EventJob.class);

  @Autowired private CollectionExerciseEventPublisher eventPublisher;

  @Override
  public void execute(JobExecutionContext jec) throws JobExecutionException {
    JobDetail jobDetail = jec.getJobDetail();
    JobDataMap dataMap = jobDetail.getJobDataMap();
    String tag = dataMap.getString(SchedulerConfiguration.DataKey.tag.name());
    UUID collexId =
        UUID.fromString(
            dataMap.getString(SchedulerConfiguration.DataKey.collectionExercise.name()));
    UUID eventId = UUID.fromString(dataMap.getString(SchedulerConfiguration.DataKey.id.name()));
    Date date = new Date(dataMap.getLong(SchedulerConfiguration.DataKey.timestamp.name()));
    log.with("tag", tag)
        .with("collection_exercise_id", collexId)
        .with("date", date)
        .with("job-key", jobDetail.getKey())
        .info("Executing event");

    try {
      EventDTO event = new EventDTO();
      event.setId(eventId);
      event.setCollectionExerciseId(collexId);
      event.setTag(tag);
      event.setTimestamp(date);

      this.eventPublisher.publishCollectionExerciseEvent(
          CollectionExerciseEventPublisher.MessageType.EventElapsed, event);
    } catch (CTPException e) {
      String message = String.format("Error publishing collection exercise event %s", eventId);
      throw new JobExecutionException(message, e);
    }
  }
}
