package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.tools.rabbit.Rabbitmq;

/**
 * The apps main holder for centralised configuration read from application.yml or environment
 * variables.
 */
@CoverageIgnore
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {

  private ActionSvc actionSvc;
  private SampleSvc sampleSvc;
  private SurveySvc surveySvc;
  private CollectionInstrumentSvc collectionInstrumentSvc;
  private PartySvc partySvc;
  private RedissonConfig redissonConfig;
  private ScheduleSettings schedules;
  private SwaggerSettings swaggerSettings;
  private Rabbitmq rabbitmq;
}
