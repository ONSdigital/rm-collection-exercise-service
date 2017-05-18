package uk.gov.ons.ctp.response.collection.exercise;

import java.text.ParseException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
import uk.gov.ons.ctp.response.collection.exercise.message.impl.SendToCaseImpl;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
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
  private SendToCaseImpl sendToCase;

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
  public RestClient sampleSvcClientRestTemplate() {
    RestClient restHelper = new RestClient(appConfig.getSampleSvc().getConnectionConfig());
    return restHelper;
  }

  @Bean
  public CommandLineRunner linkViaActionId(){

      return (args) -> {
    	 
    	  sendToCase.send();
      };

  }
  /**
   * Bean to allow controlled state transitions of CollectionExercises.
   *
   * @return the state transition manager specifically for CollectionExercises
   */
  @SuppressWarnings("unchecked")
  @Bean
  public StateTransitionManager<CollectionExerciseState, CollectionExerciseEvent> collectionExerciseSvcStateTransitionManager() {
    return collectionExerciseStateTransitionManagerFactory
        .getStateTransitionManager(CollectionExerciseStateTransitionManagerFactory.COLLLECTIONEXERCISE_ENTITY);
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
 * @throws ParseException 
 * @throws DatatypeConfigurationException 
   */
  public static void main(String[] args) throws DatatypeConfigurationException, ParseException {
    SpringApplication.run(CollectionExerciseApplication.class, args);
    
  }
}
