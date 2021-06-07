package uk.gov.ons.ctp.response.collection.exercise.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.lib.sampleunit.definition.SampleUnit;
import uk.gov.ons.ctp.response.collection.exercise.service.SampleService;
import uk.gov.ons.ctp.response.collection.exercise.utility.PubSubEmulator;

/** PubSub subscription responsible for receipt of sample units via PubSub. */
@Component
public class SampleUnitReceiver {
  private static final Logger log = LoggerFactory.getLogger(SampleUnitReceiver.class);
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SampleService sampleService;
  @Autowired AppConfig appConfig;

  /**
   * To process SampleUnit from PubSub This creates application ready event listener to provide an
   * active subscription for the new sample unit when published.
   *
   * @throws IOException
   */
  @EventListener(ApplicationReadyEvent.class)
  public void acceptSampleUnit() throws IOException {
    log.debug("received SampleUnit Message from PubSub");
    // Instantiate an asynchronous message receiver.
    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          // Handle incoming message, then ack the received message.
          log.with(message.getMessageId()).info("Receiving message ID from PubSub");
          log.with(message.getData().toString()).debug("Receiving data from PubSub ");
          try {
            SampleUnit sampleUnit =
                objectMapper.readValue(message.getData().toStringUtf8(), SampleUnit.class);
            sampleService.acceptSampleUnit(sampleUnit);
            consumer.ack();
          } catch (final IOException e) {
            log.with(e)
                .error(
                    "Something went wrong while processing message received from PubSub "
                        + "for sample unit notification");
            consumer.nack();
          }
        };
    Subscriber subscriber = getSampleUnitReceiverSubscriber(receiver);
    // Start the subscriber.
    subscriber.startAsync().awaitRunning();
    log.with(subscriber.getSubscriptionNameString())
        .info("Listening for sample unit notification messages on PubSub-subscription id");
  }

  /**
   * Provides PubSub subscriber for sample unit notification against message receiver
   *
   * @param receiver: com.google.cloud.pubsub.v1.MessageReceiver;
   * @return com.google.cloud.pubsub.v1.Subscriber;
   */
  private Subscriber getSampleUnitReceiverSubscriber(MessageReceiver receiver) throws IOException {
    if (StringUtil.isBlank(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      log.info("Returning Subscriber for sample unit notification");
      ExecutorProvider executorProvider =
          InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(4).build();
      // `setParallelPullCount` determines how many StreamingPull streams the subscriber will open
      // to receive message. It defaults to 1. `setExecutorProvider` configures an executor for the
      // subscriber to process messages. Here, the subscriber is configured to open 2 streams for
      // receiving messages, each stream creates a new executor with 4 threads to help process the
      // message callbacks. In total 2x4=8 threads are used for message processing.
      return Subscriber.newBuilder(getSampleUnitSubscriptionName(), receiver)
          .setParallelPullCount(2)
          .setExecutorProvider(executorProvider)
          .build();
    } else {
      log.info("Returning emulator Subscriber");
      return new PubSubEmulator().getSampleUnitEmulatorSubscriber(receiver);
    }
  }

  /**
   * * Provides subscription name for the sample unit subscriber
   *
   * @return com.google.pubsub.v1.ProjectSubscriptionName
   */
  private ProjectSubscriptionName getSampleUnitSubscriptionName() {
    String project = appConfig.getGcp().getProject();
    String subscriptionId = appConfig.getGcp().getSampleUnitReceiverSubscription();
    log.with("Subscription id", subscriptionId)
        .with("project", project)
        .info("creating pubsub subscription name for sample unit notifications ");
    return ProjectSubscriptionName.of(project, subscriptionId);
  }
}
