package jimlind.filmlinkd.systems.google;

import java.io.IOException;

import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Subscription.Builder;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GooglePubSubManager {

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
        SubscriptionAdminClient subscriptionAdminClient;

        try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
            subscriptionAdminClient = client;
        } catch (IOException e) {
            log.error("Unable to setup connection to the PubSub client", e);
            return;
        }

        try {
            subscriptionAdminClient.getSubscription(subscriptionName);
        } catch (NotFoundException e) {
            Builder builder = Subscription.newBuilder().setName(subscriptionName.toString())
                    .setTopic(topicName.toString()).setAckDeadlineSeconds(10)
                    .setMessageRetentionDuration(retentionDuration).setExpirationPolicy(expirationPolicy);
            subscriptionAdminClient.createSubscription(builder.build());
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
        log.info("Staring Listening for Messages on {}", subscriptionName.toString());
    }

    public void stop() {
        if (this.subscriber != null) {
            log.info("Stopping PubSub Listening for Messages on {}", this.subscriber.getSubscriptionNameString());
            this.subscriber.stopAsync();
        } else {
            log.info("Stopping PubSub with no Active Subscriptions");
        }

    }
}
