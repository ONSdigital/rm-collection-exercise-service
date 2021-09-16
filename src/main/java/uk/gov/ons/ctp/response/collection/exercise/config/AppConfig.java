package uk.gov.ons.ctp.response.collection.exercise.config;

import lombok.Data;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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
  private CaseSvc caseSvc;
  private SampleSvc sampleSvc;
  private SurveySvc surveySvc;
  private CollectionInstrumentSvc collectionInstrumentSvc;
  private RedissonConfig redissonConfig;
  private ScheduleSettings schedules;
  private SwaggerSettings swaggerSettings;
  private Logging logging;
  private GCP gcp;
}
