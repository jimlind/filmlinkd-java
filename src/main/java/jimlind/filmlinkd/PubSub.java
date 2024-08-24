package jimlind.filmlinkd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;

@Component
public class PubSub {
    private static final Logger logger = LoggerFactory.getLogger(PubSub.class);

    String topicId = "filmlinkd-dev-log-entry-topic";
    String subscriptionId = "filmlinkd-dev-log-entry-subscription-java";
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

        // Create the subscription
        try {
            SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
            ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
            subscriptionAdminClient.createSubscription(
                    Subscription.newBuilder().setName(subscriptionName.toString()).setTopic(topicName.toString())
                            .setAckDeadlineSeconds(10).setMessageRetentionDuration(retentionDuration)
                            .setExpirationPolicy(expirationPolicy).build());
        } catch (Exception e) {
            logger.error("Unable to create the subscription", e);
        }

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            this.queue.set(message);
            consumer.ack();
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        // Start the subscriber.
        subscriber.startAsync().awaitRunning();
        logger.info("Listening for messages on " + subscriptionName.toString());
    }
}
