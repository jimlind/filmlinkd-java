package jimlind.filmlinkd.system.google;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.*;
import java.util.Objects;
import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.listener.SubscriberListener;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.MessageReceiver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PubSubManager {
  static final int RETENTION_SECONDS = 43200; // 12 Hours
  static final int EXPIRATION_SECONDS = 86400; // 24 Hours
  static final int ACK_DEADLINE_SECONDS = 10;

  @Autowired private Config config;
  @Autowired private MessageReceiver messageReceiver;
  @Autowired private SubscriberListener subscriberListener;

  @Nullable private Publisher logEntryPublisher;
  @Nullable private Publisher commandPublisher;
  @Nullable private Subscriber logEntrySubscriber;
  @Nullable private Subscriber commandSubscriber;

  public void activate() {
    this.commandPublisher =
        buildPublisher(this.config.getGoogleProjectId(), this.config.getPubSubCommandTopicName());
    this.logEntryPublisher =
        buildPublisher(this.config.getGoogleProjectId(), this.config.getPubSubLogEntryTopicName());

    this.logEntrySubscriber =
        buildSubscriber(
            this.config.getGoogleProjectId(),
            this.config.getPubSubLogEntrySubscriptionName(),
            this.config.getPubSubLogEntryTopicName());
  }

  public void deactivate() {
    if (this.logEntrySubscriber != null) {
      log.atInfo()
          .setMessage("Stopping PubSub Subscriber for Messages on {}")
          .addArgument(this.logEntrySubscriber.getSubscriptionNameString())
          .log();
      this.logEntrySubscriber.stopAsync();
    }

    if (this.logEntryPublisher != null) {
      log.info("Stopping PubSub Publisher on {}", this.logEntryPublisher.getTopicNameString());
      this.logEntryPublisher.shutdown();
    }

    if (this.commandPublisher != null) {
      log.info("Stopping PubSub Publisher on {}", this.commandPublisher.getTopicNameString());
      this.commandPublisher.shutdown();
    }
  }

  private static Publisher buildPublisher(String projectId, String topicId) {
    TopicName topicName = TopicName.of(projectId, topicId);
    try {
      return Publisher.newBuilder(topicName).build();
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to build Publisher {}")
          .addArgument(topicId)
          .addKeyValue("exception", e)
          .log();
      return null;
    }
  }

  private Subscriber buildSubscriber(String projectId, String subscriptionId, String topicId) {
    ProjectName projectName = ProjectName.of(projectId);
    TopicName topicName = TopicName.of(projectId, topicId);
    SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);

    // This client create is designed specifically for a try-with-resources statement
    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      // If the subscription doesn't exit, create it.
      if (!hasSubscription(client, projectName, subscriptionName)) {
        createSubscription(client, subscriptionName, topicName);
      }
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to setup connection to the PubSub client")
          .addKeyValue("exception", e)
          .log();
      return null;
    }

    // Build a subscriber wired up to a message receivers and event listeners
    // If we are going to properly expand this to be a method that creates generic subscribers this
    // will have to be rebuilt.
    Subscriber subscriber =
        Subscriber.newBuilder(subscriptionName.toString(), this.messageReceiver).build();
    subscriber.addListener(this.subscriberListener, MoreExecutors.directExecutor());

    subscriber.startAsync().awaitRunning();
    log.info("Starting Listening for Messages on {}", subscriptionName);

    return subscriber;
  }

  public void publishCommand(Command command) {
    if (this.commandPublisher == null) {
      return;
    }

    ByteString data = ByteString.copyFromUtf8(command.toJson());
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
    try {
      this.commandPublisher.publish(pubsubMessage).get();
    } catch (Exception e) {
      log.atWarn()
          .setMessage("Unable to Publish command")
          .addKeyValue("command", command)
          .addKeyValue("exception", e)
          .log();
    }
  }

  public void publishLogEntry(Message logEntry) {
    if (this.logEntryPublisher == null) {
      return;
    }

    ByteString data = ByteString.copyFromUtf8(logEntry.toJson());
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
    try {
      this.logEntryPublisher.publish(pubsubMessage).get();
    } catch (Exception e) {
      log.atWarn()
          .setMessage("Unable to Publish logEntry")
          .addKeyValue("logEntry", logEntry)
          .addKeyValue("exception", e)
          .log();
    }
  }

  private static boolean hasSubscription(
      SubscriptionAdminClient client, ProjectName projectName, SubscriptionName subscriptionName) {
    String project = projectName.toString();
    for (Subscription subscription : client.listSubscriptions(project).iterateAll()) {
      if (Objects.equals(subscription.getName(), subscriptionName.toString())) {
        return true;
      }
    }
    return false;
  }

  private static void createSubscription(
      SubscriptionAdminClient client, SubscriptionName subscriptionName, TopicName topicName) {
    // Setup duration and expirations
    Duration retentionDuration = Duration.newBuilder().setSeconds(RETENTION_SECONDS).build();
    Duration expirationDuration = Duration.newBuilder().setSeconds(EXPIRATION_SECONDS).build();
    ExpirationPolicy expirationPolicy =
        ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();

    Subscription.Builder builder =
        Subscription.newBuilder()
            .setName(subscriptionName.toString())
            .setTopic(topicName.toString())
            .setAckDeadlineSeconds(ACK_DEADLINE_SECONDS)
            .setMessageRetentionDuration(retentionDuration)
            .setExpirationPolicy(expirationPolicy);

    client.createSubscription(builder.build());
  }
}
