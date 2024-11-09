package jimlind.filmlinkd.system.google;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.*;
import com.google.pubsub.v1.Subscription.Builder;
import java.util.Objects;
import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.listener.SubscriberListener;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.MessageReceiver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PubSubManager {
  private final Config config;
  private final MessageReceiver messageReceiver;
  private final SubscriberListener subscriberListener;

  private final Duration retentionDuration = Duration.newBuilder().setSeconds(43200).build();
  private final Duration expirationDuration = Duration.newBuilder().setSeconds(86400).build();
  private final ExpirationPolicy expirationPolicy =
      ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();

  private final TopicName logEntryTopicName;
  private final SubscriptionName logEntrySubscriptionName;
  private final TopicName commandTopicName;
  private final SubscriptionName commandSubscriptionName;
  private Subscriber subscriber;

  @Autowired
  public PubSubManager(
      Config config, MessageReceiver messageReceiver, SubscriberListener subscriberListener) {
    this.config = config;
    this.messageReceiver = messageReceiver;
    this.subscriberListener = subscriberListener;

    String projectId = this.config.getGoogleProjectId();
    String pubSubLogEntryTopic = this.config.getPubSubLogEntryTopicName();
    String pubSubLogEntrySubscription = this.config.getPubSubLogEntrySubscriptionName();
    String pubSubCommandTopic = this.config.getPubSubCommandTopicName();
    String pubSubCommandSubscription = this.config.getPubSubCommandSubscriptionName();

    this.logEntryTopicName = TopicName.of(projectId, pubSubLogEntryTopic);
    this.logEntrySubscriptionName = SubscriptionName.of(projectId, pubSubLogEntrySubscription);
    this.commandTopicName = TopicName.of(projectId, pubSubCommandTopic);
    this.commandSubscriptionName = SubscriptionName.of(projectId, pubSubCommandSubscription);
  }

  public void startListening() {
    // This client create is designed specifically for a try-with-resources statement
    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      // If the subscription doesn't exit, create it.
      if (!hasSubscription(client, logEntrySubscriptionName.toString())) {
        createSubscription(client, logEntrySubscriptionName, logEntryTopicName);
      }
    } catch (Exception e) {
      log.error("Unable to setup connection to the PubSub client", e);
      return;
    }

    // Build a subscriber wired up to a message receivers and event listeners
    this.subscriber =
        Subscriber.newBuilder(logEntrySubscriptionName.toString(), this.messageReceiver).build();
    subscriber.addListener(this.subscriberListener, MoreExecutors.directExecutor());

    this.subscriber.startAsync().awaitRunning();
    log.info("Staring Listening for Messages on {}", logEntrySubscriptionName);
  }

  public void stopListening() {
    if (this.subscriber != null) {
      log.info(
          "Stopping PubSub Listening for Messages on {}",
          this.subscriber.getSubscriptionNameString());
      this.subscriber.stopAsync();
    } else {
      log.info("Stopping PubSub with no Active Subscriptions");
    }
  }

  public void publishCommand(Command command) {
    try {
      ByteString data = ByteString.copyFromUtf8(command.toJson());
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      Publisher.newBuilder(this.commandTopicName).build().publish(pubsubMessage).get();
    } catch (Exception e) {
      log.atWarn()
          .setMessage("Unable to publish command")
          .addKeyValue("command", command)
          .addKeyValue("exception", e)
          .log();
    }
  }

  public void publishLogEntry(Message logEntry) {
    try {
      ByteString data = ByteString.copyFromUtf8(logEntry.toJson());
      PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
      Publisher.newBuilder(this.logEntryTopicName).build().publish(pubsubMessage).get();
    } catch (Exception e) {
      log.atWarn()
          .setMessage("Unable to publish logEntry")
          .addKeyValue("logEntry", logEntry)
          .addKeyValue("exception", e)
          .log();
    }
  }

  private void createSubscription(
      SubscriptionAdminClient client, SubscriptionName subscriptionName, TopicName topicName) {
    Builder builder =
        Subscription.newBuilder()
            .setName(subscriptionName.toString())
            .setTopic(topicName.toString())
            .setAckDeadlineSeconds(10)
            .setMessageRetentionDuration(retentionDuration)
            .setExpirationPolicy(expirationPolicy);
    client.createSubscription(builder.build());
  }

  private boolean hasSubscription(SubscriptionAdminClient client, String subscriptionName) {
    String project = ProjectName.of(this.config.getGoogleProjectId()).toString();
    for (Subscription subscription : client.listSubscriptions(project).iterateAll()) {
      if (Objects.equals(subscription.getName(), subscriptionName)) {
        return true;
      }
    }
    return false;
  }
}
