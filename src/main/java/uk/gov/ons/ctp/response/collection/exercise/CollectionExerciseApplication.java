package uk.gov.ons.ctp.response.collection.exercise;

import com.godaddy.logging.LoggingConfigs;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.cobertura.CoverageIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.error.RestExceptionHandler;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.jackson.CustomObjectMapper;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.rest.RestUtility;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManager;
import uk.gov.ons.ctp.response.collection.exercise.lib.common.state.StateTransitionManagerFactory;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseEvent;
import uk.gov.ons.ctp.response.collection.exercise.representation.CollectionExerciseDTO.CollectionExerciseState;
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
@EnableScheduling
@EnableCaching
@Slf4j
public class CollectionExerciseApplication {

  @Autowired private AppConfig appConfig;

  @Autowired private DataSource dataSource;

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

  @Bean
  public LiquibaseProperties liquibaseProperties() {
    return new LiquibaseProperties();
  }

  @Bean
  @DependsOn(value = "entityManagerFactory")
  @DependsOnDatabaseInitialization
  public CustomSpringLiquibase liquibase() {
    LiquibaseProperties liquibaseProperties = liquibaseProperties();
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setChangeLog(liquibaseProperties.getChangeLog());
    liquibase.setDataSource(getDataSource(liquibaseProperties));
    liquibase.setDropFirst(liquibaseProperties.isDropFirst());
    liquibase.setShouldRun(true);
    liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
    liquibase.setLiquibaseSchema(liquibaseProperties.getLiquibaseSchema());
    liquibase.setDatabaseChangeLogLockTable(liquibaseProperties.getDatabaseChangeLogLockTable());
    liquibase.setDatabaseChangeLogTable(liquibaseProperties.getDatabaseChangeLogTable());
    return new CustomSpringLiquibase(liquibase);
  }

  private DataSource getDataSource(LiquibaseProperties liquibaseProperties) {
    if (liquibaseProperties.getUrl() == null) {
      return this.dataSource;
    }
    return DataSourceBuilder.create()
        .url(liquibaseProperties.getUrl())
        .username(liquibaseProperties.getUser())
        .password(liquibaseProperties.getPassword())
        .build();
  }

  /**
   * The RestUtility bean for the Case service
   *
   * @return the RestUtility bean for the Case service
   */
  @Bean
  @Qualifier("caseRestUtility")
  public RestUtility caseRestUtility() {
    return new RestUtility(appConfig.getCaseSvc().getConnectionConfig());
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

  /*
   * PubSub / Spring integration configuration
   *
   */

  @Bean(name = "sampleSummaryStatusChannel")
  public MessageChannel inputMessageChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter inboundChannelAdapter(
      @Qualifier("sampleSummaryStatusChannel") MessageChannel messageChannel,
      PubSubTemplate pubSubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(
            pubSubTemplate, appConfig.getGcp().getSampleSummaryActivationStatusSubscription());
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  @ServiceActivator(inputChannel = "sampleSummaryActivationChannel")
  public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, appConfig.getGcp().getSampleSummaryActivationTopic());
  }

  @MessagingGateway(defaultRequestChannel = "sampleSummaryActivationChannel")
  public interface PubsubOutboundGateway {
    void sendToPubsub(String text);
  }

  @Bean
  @ServiceActivator(inputChannel = "collectionExerciseEndChannel")
  public MessageHandler collectionExerciseEndMessageSender(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(
        pubsubTemplate, appConfig.getGcp().getCollectionExerciseEndTopic());
  }

  @MessagingGateway(defaultRequestChannel = "collectionExerciseEndChannel")
  public interface CollectionExerciseEndOutboundGateway {
    void sendToPubsub(String text);
  }

  /**
   * Spring boot start-up
   *
   * @param args These are the optional command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CollectionExerciseApplication.class, args);
  }

  @PostConstruct
  public void initJsonLogging() {
    if (appConfig.getLogging().isUseJson()) {
      LoggingConfigs.setCurrent(LoggingConfigs.getCurrent().useJson());
    }
  }

  @Bean
  public PubSubInboundChannelAdapter eventStatusUpdateChannelAdapter(
      @Qualifier("collectionExerciseEventStatusUpdateChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    String subscriptionName =
        appConfig.getGcp().getCollectionExerciseEventStatusUpdateSubscription();
    log.info(
        "Application is listening for case event status update on subscription id {}",
        subscriptionName);
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  public MessageChannel collectionExerciseEventStatusUpdateChannel() {
    return new PublishSubscribeChannel();
  }

  @Bean
  public PubSubInboundChannelAdapter supplementaryDataServiceInboundChannelAdapter(
      @Qualifier("supplementaryDataServiceMessageChannel") MessageChannel inputChannel,
      PubSubTemplate pubSubTemplate) {
    String subscriptionName = appConfig.getGcp().getSupplementaryDataServiceSubscription();
    log.info(
        "Application is listening for Supplementary Data Service requests {}", subscriptionName);
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);
    return adapter;
  }

  @Bean
  public MessageChannel supplementaryDataServiceMessageChannel() {
    return new PublishSubscribeChannel();
  }
}
