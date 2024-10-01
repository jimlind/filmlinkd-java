package jimlind.filmlinkd.system.google;

import com.google.pubsub.v1.PubsubMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;

@Component
@Slf4j
// Queue exists so that I can rate limit the amount of processing that happens.
// If we let every PubSub event trigger some logic it can take over the
// CPU really quickly.
//
// If there was some kind of throttled events in Spring I wouldn't need to do
// this. I should go look that up and see if it does exist.
public class PubSubQueue {
  private final LinkedList<PubsubMessage> messageList = new LinkedList<PubsubMessage>();
  private final ArrayList<Integer> fetchIdList = new ArrayList<Integer>();

  public void set(PubsubMessage message) {
    messageList.add(message);
  }

  public synchronized PubsubMessage get(Integer fetchClientId, Integer fetchClientTotal) {
    // Check if the specific ID was used for fetching and set it otherwise
    if (this.fetchIdList.contains(fetchClientId)) {
      return null;
    }

    // Get the first message from the queue
    // Checking length doesn't seem to be a foolproof way to resolve this so wrapping in a try/catch
    PubsubMessage message;
    try {
      message = messageList.getFirst();
    } catch (Exception e) {
      return null;
    }

    // Indicate the fetch Id is used
    this.fetchIdList.add(fetchClientId);

    // Remove the first message because it's been fetched by all parties
    if (this.fetchIdList.size() == fetchClientTotal) {
      messageList.removeFirst();
      this.fetchIdList.clear();
    }

    return message;
  }
}
