package uk.gov.ons.ctp.response.collection.exercise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * The apps main holder for centralised configuration read from application.yml
 * or environment variables.
 *
 */
@Configuration
@ConfigurationProperties
@Data
public class AppConfig {
  private SampleSvc sampleSvc;

}
