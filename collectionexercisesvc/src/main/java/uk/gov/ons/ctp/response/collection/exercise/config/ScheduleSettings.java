package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;

/**
 * Config POJO for Scheduled threads
 *
 */
@Data
public class ScheduleSettings {
    private String validationScheduleDelayMilliSeconds;
    private Integer validationScheduleRetrievalMax;
    private String distributionScheduleDelayMilliSeconds;
    private Integer distributionScheduleRetrievalMax;
}
