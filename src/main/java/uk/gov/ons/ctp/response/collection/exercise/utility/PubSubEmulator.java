package uk.gov.ons.ctp.response.collection.exercise.utility;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * * This is a PubSub Emulator class. This is a utility class which is used for testing pubsub
 * function
 */
public class PubSubEmulator {
  private static final Logger log = LoggerFactory.getLogger(PubSubEmulator.class);
  private static final String HOST_PORT = "localhost:18681";
  public static final ManagedChannel CHANNEL =
      ManagedChannelBuilder.forTarget(HOST_PORT).usePlaintext().build();
  public static final TransportChannelProvider CHANNEL_PROVIDER =
      FixedTransportChannelProvider.create(GrpcTransportChannel.create(CHANNEL));
  public static final CredentialsProvider CREDENTIAL_PROVIDER = NoCredentialsProvider.create();
  private static final String PROJECT_ID = "test";
  private static final String TOPIC_ID = "test_topic";
  private static final String SUBSCRIPTION_ID = "test_subscription";
  private final TopicAdminClient topicClient =
      TopicAdminClient.create(
          TopicAdminSettings.newBuilder()
              .setTransportChannelProvider(PubSubEmulator.CHANNEL_PROVIDER)
              .setCredentialsProvider(PubSubEmulator.CREDENTIAL_PROVIDER)
              .build());
  SubscriptionAdminClient subscriptionAdminClient =
      SubscriptionAdminClient.create(
          SubscriptionAdminSettings.newBuilder()
              .setTransportChannelProvider(PubSubEmulator.CHANNEL_PROVIDER)
              .setCredentialsProvider(PubSubEmulator.CREDENTIAL_PROVIDER)
              .build());
  TopicName topicName = TopicName.of(PROJECT_ID, TOPIC_ID);
  ProjectSubscriptionName subscriptionName =
      ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID);

  public PubSubEmulator() throws IOException {}

  public Publisher getEmulatorPublisher(TopicName topicName) throws IOException {
    return Publisher.newBuilder(topicName)
        .setChannelProvider(CHANNEL_PROVIDER)
        .setCredentialsProvider(CREDENTIAL_PROVIDER)
        .build();
  }

  public Subscriber getEmulatorSubscriber(MessageReceiver receiver) {
    return Subscriber.newBuilder(ProjectSubscriptionName.of(PROJECT_ID, SUBSCRIPTION_ID), receiver)
        .setChannelProvider(CHANNEL_PROVIDER)
        .setCredentialsProvider(CREDENTIAL_PROVIDER)
        .build();
  }

  public Subscriber getSampleUnitEmulatorSubscriber(MessageReceiver receiver) {
    return Subscriber.newBuilder(
            ProjectSubscriptionName.of(PROJECT_ID, "sample_unit_subscription"), receiver)
        .setChannelProvider(CHANNEL_PROVIDER)
        .setCredentialsProvider(CREDENTIAL_PROVIDER)
        .build();
  }

  public GrpcSubscriberStub getEmulatorSubscriberStub() throws IOException {
    return GrpcSubscriberStub.create(
        SubscriberStubSettings.newBuilder()
            .setTransportChannelProvider(CHANNEL_PROVIDER)
            .setCredentialsProvider(CREDENTIAL_PROVIDER)
            .build());
  }

  public void publishMessage(String message, String topicId) {
    try {
      ByteString data = ByteString.copyFromUtf8(message);
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      TopicName topicName = TopicName.of(PROJECT_ID, topicId);
      Publisher publisher = getEmulatorPublisher(topicName);
      log.with("publisher", publisher).info("Publishing message to pubsub emulator");
      ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
      String messageId = messageIdFuture.get();
      log.with("messageId", messageId).info("Published message to pubsub emulator");
    } catch (IOException | InterruptedException | ExecutionException e) {
      log.error("Failed to publish message", e);
    }
  }

  public void shutdown() {
    CHANNEL.shutdown();
  }

  public void testInit() {
    Topic topic = topicClient.createTopic(topicName);
    System.out.println("Created topic: " + topic.getName());
    Subscription subscription =
        subscriptionAdminClient.createSubscription(
            subscriptionName, topicName, PushConfig.getDefaultInstance(), 10);
    System.out.println("Created pull subscription: " + subscription.getName());
  }

  public void testTeardown() {
    subscriptionAdminClient.deleteSubscription(subscriptionName);
    topicClient.deleteTopic(topicName);
  }
}
