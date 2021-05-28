package uk.gov.ons.ctp.response.collection.exercise.message;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import jodd.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.ctp.response.collection.exercise.config.AppConfig;
import uk.gov.ons.ctp.response.collection.exercise.utility.PubSubEmulator;

@Component
public class PubSub {
  private static final Logger log = LoggerFactory.getLogger(PubSub.class);
  @Autowired AppConfig appConfig;

  private Publisher publisherSupplier(String project, String topic) throws IOException {
    log.info("creating pubsub publish for topic " + topic + " in project " + project);
    TopicName topicName = TopicName.of(project, topic);
    if (StringUtil.isBlank(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      log.info("Returning actual Publisher");
      return Publisher.newBuilder(topicName).build();
    } else {
      log.with("PubSub emulator host", System.getenv("PUBSUB_EMULATOR_HOST"))
          .info("Returning emulator Publisher");
      log.info("Returning emulator Publisher");
      return new PubSubEmulator().getEmulatorPublisher(topicName);
    }
  }

  public Publisher sampleUnitPublisher() throws IOException {
    return publisherSupplier(
        appConfig.getGcp().getProject(), appConfig.getGcp().getCaseNotificationTopic());
  }

  public void shutdown() {
    if (StringUtil.isEmpty(System.getenv("PUBSUB_EMULATOR_HOST"))) {
      PubSubEmulator.CHANNEL.shutdown();
    }
  }
}
