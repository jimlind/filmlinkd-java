package jimlind.filmlinkd;

import java.util.ArrayList;
import java.util.LinkedList;

import org.springframework.stereotype.Component;

import com.google.pubsub.v1.PubsubMessage;

@Component
// Queue exists so that I can rate limit the amount of processing that happens.
// If we let every PubSub event trigger some logic it can take over the
// CPU really quickly.
//
// If there was some kind of throttled events in Spring I wouldn't need to do
// this. I should go look that up and see if it does exist.
public class Queue {
    public Boolean writeOnlyLock = true;

    private final LinkedList<PubsubMessage> messageList = new LinkedList<PubsubMessage>();
    private final ArrayList<Integer> fetchIdList = new ArrayList<Integer>();

    public void set(PubsubMessage message) {
        messageList.add(message);
    }

    // This should be fixed probably to return a null instead of an exception that needs to get caught since the early
    // exits are business as usual, but I wasn't thinking about that PubsubMessage|null as acceptable return types
    public synchronized PubsubMessage get(Integer fetchClientId, Integer fetchClientTotal) throws Exception {
        // Check if the specific ID was used for fetching and set it otherwise
        if (this.fetchIdList.contains(fetchClientId)) {
            throw new Exception("Already Fetched this Resource");
        }

        // Don't allow reading until all fetching clients have connected
        if (writeOnlyLock) {
            throw new Exception("Not Ready");
        }

        // Don't return anything if nothing to return
        if (messageList.isEmpty()) {
            throw new Exception("Nothing Available");
        }

        // Indicate the fetch Id is used
        this.fetchIdList.add(fetchClientId);

        // Get the first message from the queue
        PubsubMessage message = messageList.getFirst();

        // Remove the first message because it's been fetched by all parties
        if (this.fetchIdList.size() == fetchClientTotal) {
            messageList.removeFirst();
            this.fetchIdList.clear();
        }

        return message;
    }
}
