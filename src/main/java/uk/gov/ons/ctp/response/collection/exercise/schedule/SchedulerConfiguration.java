package uk.gov.ons.ctp.response.collection.exercise.schedule;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import uk.gov.ons.ctp.response.collection.exercise.domain.Event;
import uk.gov.ons.ctp.response.collection.exercise.message.dto.ScheduledEventDTO;
import uk.gov.ons.ctp.response.collection.exercise.representation.EventDTO;
import uk.gov.ons.ctp.response.collection.exercise.service.EventService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
@Slf4j
public class SchedulerConfiguration {

    @Autowired
    private EventService eventService;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Method to schedule a collection exercise event
     *
     * @param scheduler a Quartz scheduler (can be autowired)
     * @param event     the collection exercise event to schedule
     * @return the date and time the event is scheduled for
     * @throws SchedulerException thrown if an error occurred scheduling event
     */
    public static Date scheduleEvent(final Scheduler scheduler, final Event event) throws SchedulerException {
        EventJobTriggerDetail detail = new EventJobTriggerDetail(event);
        JobKey jobKey = detail.getJobDetail().getKey();

        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
        }

        return scheduler.scheduleJob(detail.getJobDetail(), detail.getTrigger());
    }

    /**
     * Method to unschedule a collection exercise event
     *
     * @param scheduler a Quartz scheduler (can be autowired)
     * @param event     the collection exercise event to unschedule
     * @return true if the event was unscheduled, false otherwise
     * @throws SchedulerException thrown if an error occurred unscheduling event
     */
    public static boolean unscheduleEvent(final Scheduler scheduler, final Event event) throws SchedulerException {
        EventJobTriggerDetail detail = new EventJobTriggerDetail(event);
        JobKey jobKey = detail.getJobDetail().getKey();

        return scheduler.deleteJob(jobKey);
    }

    /**
     * Utility method to generate a JobKey from a collection exercise id and an event
     *
     * @param collexId a collection exercise id
     * @param event    an event
     * @return a String that can be used as a JobKey
     */
    public static String getJobKey(final UUID collexId, final Event event) {
        return getJobKey(collexId, event.getTag());
    }

    /**
     * Utility method to generate a JobKey from a collection exercise id and an event tag
     *
     * @param collexId a collection exercise id
     * @param eventTag    an event tag
     * @return a String that can be used as a JobKey
     */
    public static String getJobKey(final UUID collexId, final String eventTag) {
        String jobKey = eventTag + ":" + collexId.toString();

        return jobKey;
    }

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

    /**
     * Schedules within quartz all the collection exercise events for which a message has not already been sent
     *
     * @param scheduler the scheduler to use to schedule the events
     */
    protected void scheduleOutstandingEvents(final Scheduler scheduler) {
        List<Event> outstandingEvents = this.eventService.getOutstandingEvents();

        outstandingEvents.stream().forEach(e -> {
            try {
                scheduleEvent(scheduler, e);
            } catch (SchedulerException e1) {
                throw new RuntimeException(e1);
            }
        });
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

        scheduleOutstandingEvents(scheduler);

        log.debug("Starting Scheduler threads");
        scheduler.start();
        return scheduler;
    }

    @Bean
    public FanoutExchange fanout() {
        return new FanoutExchange("collex-event-message-outbound-exchange");
    }

    public enum DataKey {
        tag,
        collectionExercise,
        id,
        eventPk,
        timestamp
    }

    @Data
    private static class EventJobTriggerDetail {
        private Trigger trigger;
        private JobDetail jobDetail;

        public EventJobTriggerDetail(final Event event) {
            UUID collexId = event.getCollectionExercise().getId();
            String jobKey = getJobKey(collexId, event);
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
    }

    private static EventDTO getEventDTOFromJobKey(Scheduler scheduler, JobKey jobKey) {
        try {
            EventDTO result = new EventDTO();
            JobDetail detail = scheduler.getJobDetail(jobKey);
            JobDataMap jobDataMap = detail.getJobDataMap();

            String tagStr = jobDataMap.getString(DataKey.tag.name());
            result.setTag(tagStr);

            String collexIdStr = jobDataMap.getString(DataKey.collectionExercise.name());
            UUID collexId = UUID.fromString(collexIdStr);
            result.setCollectionExerciseId(collexId);

            String eventIdStr = jobDataMap.getString(DataKey.id.name());
            result.setId(UUID.fromString(eventIdStr));

            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(getJobKey(collexId, tagStr)));
            result.setTimestamp(trigger.getStartTime());

            return result;
        } catch(SchedulerException e){
            log.error("Failed to get event information for job with key {} - {}", jobKey, e.getMessage());

            return null;
        }
    }

    private static List<EventDTO> getEventDTOsFromJobGroupName(Scheduler scheduler, String groupName){
        try {
            return scheduler
                    .getJobKeys(GroupMatcher.jobGroupEquals(groupName))
                    .stream()
                    .map(jobKey -> getEventDTOFromJobKey(scheduler, jobKey))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (SchedulerException e) {
            log.error("Failed to get job keys for group {} - {}", groupName, e.getMessage());

            return null;
        }
    }

    /**
     * Returns all the events scheduled in the supplied quartz scheduler
     * @param scheduler
     * @return a list of ScheduledEventDTO
     */
    public static List<EventDTO> getAllScheduledEvents(final Scheduler scheduler) throws SchedulerException {
        return scheduler.getJobGroupNames()
                .stream()
                .map(name -> getEventDTOsFromJobGroupName(scheduler, name))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
