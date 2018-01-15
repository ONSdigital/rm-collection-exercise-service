package uk.gov.ons.ctp.response.collection.exercise.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.common.error.CTPException;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class EventJob implements Job {
    @Autowired
    private EventService eventService;

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDetail jobDetail = jec.getJobDetail();
        JobDataMap dataMap = jobDetail.getJobDataMap();
        String tag = dataMap.getString(SchedulerConfiguration.DataKey.tag.name());
        UUID collexId = UUID.fromString(dataMap.getString(SchedulerConfiguration.DataKey.collectionExercise.name()));
        UUID eventId = UUID.fromString(dataMap.getString(SchedulerConfiguration.DataKey.id.name()));
        Date date = new Date(dataMap.getLong(SchedulerConfiguration.DataKey.timestamp.name()));
        log.info("Executing event: {} - {} - {}", tag, collexId, date);

        try {
            this.eventService.setEventMessageSent(eventId);
        } catch (CTPException e) {
            String message = String.format("Failed to set event %s as message sent", eventId);
            log.error(message);

            throw new JobExecutionException(message, e);
        }
    }
}
