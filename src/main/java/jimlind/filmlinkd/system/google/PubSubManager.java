package jimlind.filmlinkd.system.google;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.*;
import com.google.pubsub.v1.Subscription.Builder;
import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class PubSubManager {

    @Autowired
    private Config config;

    @Autowired
    private Queue queue;

    private final Duration retentionDuration = Duration.newBuilder().setSeconds(43200).build();
    private final Duration expirationDuration = Duration.newBuilder().setSeconds(86400).build();
    private final ExpirationPolicy expirationPolicy = ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();
    private Subscriber subscriber;

    public void start() {
        String projectId = this.config.getGoogleProjectId();
        String pubSubTopic = this.config.getPubSubLogEntryTopicName();
        String pubSubSubscription = this.config.getPubSubLogEntrySubscriptionName();

        TopicName topicName = TopicName.of(projectId, pubSubTopic);
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, pubSubSubscription);

        // This client create is designed specifically for a try-with-resources statement
        try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
            // If the subscription doesn't exit, create it.
            if (!hasSubscription(client, subscriptionName.toString())) {
                createSubscription(client, subscriptionName, topicName);
            }
        } catch (Exception e) {
            log.error("Unable to setup connection to the PubSub client", e);
            return;
        }

        // Create an asynchronous message receiver
        // This writes to a queue so that I can rate limit the amount of processing that
        // happens. If we let every PubSub event trigger some logic it can take over the
        // CPU really quickly.
        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            this.queue.set(message);
            consumer.ack();
        };

        // Wire the receiver to the subscription
        this.subscriber = Subscriber.newBuilder(subscriptionName.toString(), receiver).build();
        this.subscriber.startAsync().awaitRunning();
        log.info("Staring Listening for Messages on {}", subscriptionName);
    }

    public void stop() {
        if (this.subscriber != null) {
            log.info("Stopping PubSub Listening for Messages on {}", this.subscriber.getSubscriptionNameString());
            this.subscriber.stopAsync();
        } else {
            log.info("Stopping PubSub with no Active Subscriptions");
        }

    }

    private void createSubscription(SubscriptionAdminClient client, SubscriptionName subscriptionName, TopicName topicName) {
        Builder builder = Subscription.newBuilder().setName(subscriptionName.toString())
            .setTopic(topicName.toString()).setAckDeadlineSeconds(10)
            .setMessageRetentionDuration(retentionDuration).setExpirationPolicy(expirationPolicy);
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
