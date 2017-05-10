package uk.gov.ons.ctp.response.collection.exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;

/**
 * The main entry point into the Collection Exercise Service SpringBoot
 * Application. Also used for bean configuration.
 */
@SpringBootApplication
@EnableTransactionManagement
@IntegrationComponentScan
@ImportResource("springintegration/main.xml")
public class CollectionExerciseApplication {

  @Autowired
  private AppConfig appConfig;

  /**
   * Bean used to map exceptions for endpoints
   *
   * @return the service client
   */
  @Bean
  public RestExceptionHandler restExceptionHandler() {
    return new RestExceptionHandler();
  }

  /**
   * Bean used to access Sample service through REST calls
   *
   * @return the service client
   */
  @Bean
  public RestClient caseClient() {
    RestClient restHelper = new RestClient(appConfig.getSampleSvc().getConnectionConfig());
    return restHelper;
  }

  /**
   * Spring boot start-up
   */
  public static void main(String[] args) {
    SpringApplication.run(CollectionExerciseApplication.class, args);
  }
}
