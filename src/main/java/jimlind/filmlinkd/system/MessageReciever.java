package jimlind.filmlinkd.system;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.system.google.PubSubQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReciever implements MessageReceiver {
  @Autowired private PubSubQueue pubSubQueue;

  @Override
  // This writes to a queue so that I can rate limit the amount of processing that
  // happens. If we let every PubSub event trigger some logic it can take over the
  // CPU really quickly.
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    this.pubSubQueue.set(pubsubMessage);
    ackReplyConsumer.ack();
  }
}
