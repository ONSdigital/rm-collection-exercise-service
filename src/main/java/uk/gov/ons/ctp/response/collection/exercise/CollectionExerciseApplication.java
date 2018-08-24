package uk.gov.ons.ctp.response.collection.exercise;

import com.godaddy.logging.LoggingConfigs;
import net.sourceforge.cobertura.CoverageIgnore;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.common.distributed.DistributedListManager;
import uk.gov.ons.ctp.common.distributed.DistributedListManagerRedissonImpl;
import uk.gov.ons.ctp.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.common.rest.RestUtility;
import uk.gov.ons.ctp.common.state.StateTransitionManager;
import uk.gov.ons.ctp.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.SampleUnitGroupDTO.SampleUnitGroupState;
import uk.gov.ons.ctp.response.collection.exercise.service.BusinessEventValidator;
import uk.gov.ons.ctp.response.collection.exercise.service.EventValidator;
import uk.gov.ons.ctp.response.collection.exercise.state.CollectionExerciseStateTransitionManagerFactory;

/**
 * The main entry point into the Collection Exercise Service SpringBoot Application. Also used for
 * bean configuration.
 */
@CoverageIgnore
@SpringBootApplication
@EnableTransactionManagement
@IntegrationComponentScan
@ComponentScan(basePackages = {"uk.gov.ons.ctp.response"})
@EnableJpaRepositories(basePackages = {"uk.gov.ons.ctp.response"})
@EntityScan("uk.gov.ons.ctp.response")
@ImportResource("springintegration/main.xml")
public class CollectionExerciseApplication {

  private static final String VALIDATION_LIST = "collectionexercisesvc.sample.validation";
  private static final String DISTRIBUTION_LIST = "collectionexercisesvc.sample.distribution";

  @Autowired private AppConfig appConfig;

  @Autowired private StateTransitionManagerFactory collectionExerciseStateTransitionManagerFactory;

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
   * The restTemplate bean injected in REST client classes
   *
   * @return the restTemplate used in REST calls
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * The RestUtility bean for the Action service
   *
   * @return the RestUtility bean for the Action service
   */
  @Bean
  @Qualifier("actionRestUtility")
  public RestUtility actionRestUtility() {
    return new RestUtility(appConfig.getActionSvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the CollectionInstrument service
   *
   * @return the RestUtility bean for the CollectionInstrument service
   */
  @Bean
  @Qualifier("collectionInstrumentRestUtility")
  public RestUtility collectionInstrumentRestUtility() {
    return new RestUtility(appConfig.getCollectionInstrumentSvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the Party service
   *
   * @return the RestUtility bean for the Party service
   */
  @Bean
  @Qualifier("partyRestUtility")
  public RestUtility partyRestUtility() {
    return new RestUtility(appConfig.getPartySvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the Sample service
   *
   * @return the RestUtility bean for the Sample service
   */
  @Bean
  @Qualifier("sampleRestUtility")
  public RestUtility sampleRestUtility() {
    return new RestUtility(appConfig.getSampleSvc().getConnectionConfig());
  }

  /**
   * The RestUtility bean for the Survey service
   *
   * @return the RestUtility bean for the Survey service
   */
  @Bean
  @Qualifier("surveyRestUtility")
  public RestUtility surveyRestUtility() {
    return new RestUtility(appConfig.getSurveySvc().getConnectionConfig());
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
    return collectionExerciseStateTransitionManagerFactory.getStateTransitionManager(
        CollectionExerciseStateTransitionManagerFactory.COLLLECTIONEXERCISE_ENTITY);
  }

  /**
   * Bean to allow controlled state transitions of SampleUnitGroups.
   *
   * @return the state transition manager specifically for SampleUnitGroups.
   */
  @Bean
  @Qualifier("sampleUnitGroup")
  public StateTransitionManager<SampleUnitGroupState, SampleUnitGroupEvent>
      sampleUnitGroupStateTransitionManager() {
    return collectionExerciseStateTransitionManagerFactory.getStateTransitionManager(
        CollectionExerciseStateTransitionManagerFactory.SAMPLEUNITGROUP_ENTITY);
  }

  /**
   * The DistributedListManager for sampleUnitGroup validation.
   *
   * @param redissonClient the redissonClient.
   * @return the DistributedListManager.
   */
  @Bean
  @Qualifier("validation")
  public DistributedListManager<Integer> sampleValidationListManager(
      RedissonClient redissonClient) {
    return new DistributedListManagerRedissonImpl<Integer>(
        VALIDATION_LIST,
        redissonClient,
        appConfig.getRedissonConfig().getListTimeToWaitSeconds(),
        appConfig.getRedissonConfig().getListTimeToLiveSeconds());
  }

  /**
   * The DistributedListManager for sampleUnitGroup distribution.
   *
   * @param redissonClient the redissonClient.
   * @return the DistributedListManager.
   */
  @Bean
  @Qualifier("distribution")
  public DistributedListManager<Integer> sampleDistributionListManager(
      RedissonClient redissonClient) {
    return new DistributedListManagerRedissonImpl<Integer>(
        DISTRIBUTION_LIST,
        redissonClient,
        appConfig.getRedissonConfig().getListTimeToWaitSeconds(),
        appConfig.getRedissonConfig().getListTimeToLiveSeconds());
  }

  /**
   * Bean for access to all Redisson distributed objects.
   *
   * @return RedissonClient
   */
  @Bean
  public RedissonClient redissonClient() {
    Config config = new Config();
    config.useSingleServer().setAddress(appConfig.getRedissonConfig().getAddress());
    return Redisson.create(config);
  }

  /**
   * The CustomObjectMapper to output dates in the json in our agreed format
   *
   * @return the CustomObjectMapper
   */
  @Bean
  @Primary
  public CustomObjectMapper customObjectMapper() {
    CustomObjectMapper mapper = new CustomObjectMapper();

    return mapper;
  }

  @Bean
  public EventValidator eventValidator() {
    return new BusinessEventValidator();
  }

  /**
   * Spring boot start-up
   *
   * @param args These are the optional command line arguments
   */
  public static void main(String[] args) {
    LoggingConfigs.setCurrent(LoggingConfigs.getCurrent().useJson());

    SpringApplication.run(CollectionExerciseApplication.class, args);
  }
}
