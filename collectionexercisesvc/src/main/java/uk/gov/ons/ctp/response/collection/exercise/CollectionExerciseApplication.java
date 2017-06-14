package uk.gov.ons.ctp.response.collection.exercise;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.rest.RestClient;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.state.CollectionExerciseStateTransitionManagerFactory;

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

  @Autowired
  private StateTransitionManagerFactory collectionExerciseStateTransitionManagerFactory;

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
  @Qualifier("sampleSvc")
  public RestClient sampleSvcClientRestTemplate() {
    RestClient restHelper = new RestClient(appConfig.getSampleSvc().getConnectionConfig());
    return restHelper;
  }

  /**
   * Bean used to access Survey service through REST calls
   *
   * @return the service client
   */
  @Bean
  @Qualifier("surveySvc")
  public RestClient surveySvcClientRestTemplate() {
    RestClient restHelper = new RestClient(appConfig.getSurveySvc().getConnectionConfig());
    return restHelper;
  }

  /**
   * Bean to allow controlled state transitions of CollectionExercises.
   *
   * @return the state transition manager specifically for CollectionExercises
   */
  @Bean
  @Qualifier("collectionExercise")
  public StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent>
    collectionExerciseStateTransitionManager() {
    return collectionExerciseStateTransitionManagerFactory
        .getStateTransitionManager(CollectionExerciseStateTransitionManagerFactory.COLLLECTIONEXERCISE_ENTITY);
  }

  /**
   * Bean to allow controlled state transitions of SampleUnitGroups.
   *
   * @return the state transition manager specifically for SampleUnitGroups
   */
  @Bean
  @Qualifier("sampleUnitGroup")
  public StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent> sampleUnitGroupStateTransitionManager() {
    return collectionExerciseStateTransitionManagerFactory
        .getStateTransitionManager(CollectionExerciseStateTransitionManagerFactory.SAMPLEUNITGROUP_ENTITY);
  }

  /**
   * Bean for access to all Redisson distributed objects.
   *
   * @return RedissonClient
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer()
        .setAddress(appConfig.getRedissonConfig().getAddress());
    return Redisson.create(config);
  }

  /**
   * Spring boot start-up
   *
   * @param args These are the optional command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CollectionExerciseApplication.class, args);
  }
}
