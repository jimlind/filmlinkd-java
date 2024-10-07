package jimlind.filmlinkd.system;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.factory.ScrapedResultFactory;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver implements com.google.cloud.pubsub.v1.MessageReceiver {
  @Autowired private ScrapedResultFactory scrapedResultFactory;
  @Autowired private ScrapedResultQueue scrapedResultQueue;

  @Override
  // This writes to a queue so that I can rate limit the amount of processing that
  // happens. If we let every PubSub event trigger some logic it can take over the
  // CPU really quickly.
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    ScrapedResult scrapedResult = this.scrapedResultFactory.createFromPubSubMessage(pubsubMessage);
    if (shouldBeQueued(scrapedResult)) {
      this.scrapedResultQueue.set(scrapedResult);
    }
    ackReplyConsumer.ack();
  }

  // We expect duplicates to come in from the PubSub queue all the time so we need to limit when we
  // actually want them to be put in the queue for processing.
  private boolean shouldBeQueued(ScrapedResult scrapedResult) {
    String newLid = scrapedResult.message.entry.lid;
    String previousLid = scrapedResult.user.previous.lid;

    if (scrapedResult.message.hasChannelOverride()) {
      return true;
    }

    return LidComparer.compare(newLid, previousLid) > 1;
  }
}
