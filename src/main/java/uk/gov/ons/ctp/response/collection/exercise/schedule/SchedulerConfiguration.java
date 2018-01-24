package uk.gov.ons.ctp.response.collection.exercise.schedule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
@Slf4j
public class SchedulerConfiguration {

    @Autowired
    private EventService eventService;

    public enum DataKey {
        tag,
        collectionExercise,
        id,
        eventPk,
        timestamp
    };

    @Data
    private static class EventJobTriggerDetail {
        public EventJobTriggerDetail(final Event event){
            UUID collexId = event.getCollectionExercise().getId();
            String jobKey = event.getTag() + ":" + collexId.toString();
            JobDetail job = newJob()
                    .ofType(EventJob.class)
                    .withIdentity(JobKey.jobKey(jobKey))
                    .withDescription("Executing event " + jobKey + " at " + event.getTimestamp())
                    .usingJobData(DataKey.tag.name(), event.getTag())
                    .usingJobData(DataKey.collectionExercise.name(), event.getCollectionExercise().getId().toString())
                    .usingJobData(DataKey.id.name(), event.getId().toString())
                    .usingJobData(DataKey.eventPk.name(), event.getEventPK())
                    .usingJobData(DataKey.timestamp.name(), event.getTimestamp().getTime())
                    .build();
            Trigger trigger = newTrigger()
                    .forJob(job)
                    .withIdentity(TriggerKey.triggerKey(jobKey))
                    .withDescription("Triggering event " + jobKey + " at " + event.getTimestamp())
                    .startAt(new Date(event.getTimestamp().getTime()))
                    .build();

            this.trigger = trigger;
            this.jobDetail = job;
        }

        private Trigger trigger;
        private JobDetail jobDetail;
    }

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.info("Hello world from Quartz...");
    }

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        log.debug("Configuring Job factory");

        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    public static Date scheduleEvent(Scheduler scheduler, Event event) throws SchedulerException {
        EventJobTriggerDetail detail = new EventJobTriggerDetail(event);

        return scheduler.scheduleJob(detail.getJobDetail(), detail.getTrigger());
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException, IOException {
        // Force creation of the fanout exchange before any jobs are scheduled
        fanout();

        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize(new ClassPathResource("quartz.properties").getInputStream());

        log.debug("Getting a handle to the Scheduler");
        Scheduler scheduler = factory.getScheduler();
        scheduler.setJobFactory(springBeanJobFactory());

        this.eventService.getOutstandingEvents().stream().map(e -> {
            try {
                return scheduleEvent(scheduler, e);
            } catch (SchedulerException e1) {
                throw new RuntimeException(e1);
            }
        });

        log.debug("Starting Scheduler threads");
        scheduler.start();
        return scheduler;
    }

    @Bean
    public FanoutExchange fanout() {
        return new FanoutExchange("collex-event-message-outbound-exchange");
    }
}
