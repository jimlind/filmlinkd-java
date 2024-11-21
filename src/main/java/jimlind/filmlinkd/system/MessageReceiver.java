package jimlind.filmlinkd.system;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.EntryCache;
import jimlind.filmlinkd.factory.ScrapedResultFactory;
import jimlind.filmlinkd.model.ScrapedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageReceiver implements com.google.cloud.pubsub.v1.MessageReceiver {
  @Autowired private EntryCache entryCache;
  @Autowired private ScrapedResultFactory scrapedResultFactory;
  @Autowired private ScrapedResultQueue scrapedResultQueue;

  @Override
  // This writes to a queue so that I can rate limit the amount of processing that
  // happens. If we let every PubSub event trigger some logic it can take over the
  // CPU really quickly.
  public void receiveMessage(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer) {
    // Immediately ack the message. It'll get sent again eventually if it doesn't register.
    ackReplyConsumer.ack();

    String messagePayload = pubsubMessage.getData().toStringUtf8();
    String entryLid = getEntryLid(messagePayload);
    String channelId = getChannelId(messagePayload);

    // We are expecting multiple requests to post a diary entry so we attempt to maintain the one
    // source of truth on the server that sends messages. One mechanism of that is an memory cache.
    if (entryCache.get(entryLid) && channelId.isBlank()) {
      // If the key is in the cache and channelId isn't provided skip it
      return;
    } else {
      // Assume that write will succeed so write to cache. If it actually doesn't succeed then it'll
      // get tried again some later time, but this is designed to limit duplicates from scrape
      // events
      entryCache.set(entryLid);
    }

    ScrapedResult scrapedResult = this.scrapedResultFactory.createFromPubSubMessage(pubsubMessage);
    if (shouldBeQueued(scrapedResult)) {
      this.scrapedResultQueue.set(scrapedResult);
    }
  }

  private static String getEntryLid(String input) {
    String regex = "\"lid\":\"(\\w+)\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }

  private static String getChannelId(String input) {
    String regex = "\"channelId\":\"(\\d+)\"";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
  }

  // We expect duplicates to come in from the PubSub queue all the time so we need to limit when we
  // actually want them to be put in the queue for processing.
  private static boolean shouldBeQueued(ScrapedResult scrapedResult) {
    if (scrapedResult.message.hasChannelOverride()) {
      return true;
    }

    return !scrapedResult.user.previous.list.contains(scrapedResult.message.entry.lid);
  }
}
