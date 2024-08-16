package jimlind.filmlinkd;

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

public class PubSub {
    String projectId = "letterboxd-bot";
    String topicId = "filmlinkd-dev-log-entry-topic";
    String subscriptionId = "filmlinkd-dev-log-entry-subscription-java";
    Duration retentionDuration = Duration.newBuilder().setSeconds(43200).build();
    Duration expirationDuration = Duration.newBuilder().setSeconds(86400).build();
    ExpirationPolicy expirationPolicy = ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();

    public PubSub(Config config, Queue queue) {
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
            System.out.println(e);
        }

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        // Instantiate an asynchronous message receiver.
        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
            queue.set(message);
            consumer.ack();
        };

        Subscriber subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        // Start the subscriber.
        subscriber.startAsync().awaitRunning();
        System.out.printf("Listening for messages on %s:\n",
                subscriptionName.toString());
    }
}
