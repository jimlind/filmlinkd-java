package jimlind.filmlinkd;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Component
public class PubSub {
    private static final Logger logger = LoggerFactory.getLogger(PubSub.class);

    Duration retentionDuration = Duration.newBuilder().setSeconds(43200).build();
    Duration expirationDuration = Duration.newBuilder().setSeconds(86400).build();
    ExpirationPolicy expirationPolicy = ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();

    private final Config config;
    private final Queue queue;

    @Autowired
    public PubSub(Config config, Queue queue) {
        this.config = config;
        this.queue = queue;
    }

    public void run() {
        String projectId = this.config.getGoogleProjectId();
        String pubSubTopic = this.config.getPubSubLogEntryTopicName();
        String pubSubSubscription = this.config.getPubSubLogEntrySubscriptionName();

        TopicName topicName = TopicName.of(projectId, pubSubTopic);
        SubscriptionName subscriptionName = SubscriptionName.of(projectId, pubSubSubscription);
        SubscriptionAdminClient subscriptionAdminClient;

        try {
            subscriptionAdminClient = SubscriptionAdminClient.create();
        } catch (IOException e) {
            logger.error("Unable to setup connection to the PubSub client", e);
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

        // Setup an asynchronous message receiver
        // This writes to a queue so that I can rate limit the amount of processing that
        // happens. If we let every PubSub event trigger some logic it can take over the
        // CPU really quickly.
        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            this.queue.set(message);
            consumer.ack();
        };

        // Wire the reciever to the subscription
        Subscriber subscriber = Subscriber.newBuilder(subscriptionName.toString(), receiver).build();
        subscriber.startAsync().awaitRunning();
        logger.info("Listening for messages on " + subscriptionName.toString());

        return;
    }
}
