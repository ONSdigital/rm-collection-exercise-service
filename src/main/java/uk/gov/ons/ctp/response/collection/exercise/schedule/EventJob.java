package uk.gov.ons.ctp.response.collection.exercise.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class EventJob implements Job {
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDetail jobDetail = jec.getJobDetail();
        JobDataMap dataMap = jobDetail.getJobDataMap();
        String tag = dataMap.getString(SchedulerConfiguration.DataKey.tag.name());
        UUID collexId = UUID.fromString(dataMap.getString(SchedulerConfiguration.DataKey.collectionExercise.name()));
        Date date = new Date(dataMap.getLong(SchedulerConfiguration.DataKey.timestamp.name()));
        log.info("Executing event: {} - {} - {}", tag, collexId, date);
    }
}
