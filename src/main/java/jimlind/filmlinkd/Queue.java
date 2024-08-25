package jimlind.filmlinkd;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.google.pubsub.v1.PubsubMessage;

@Component
// Qeue exists so that I can rate limit the amount of processing that happens.
// If we let every PubSub event trigger some logic it can take over the
// CPU really quickly.
public class Queue {
    public Boolean writeOnlyLock = true;
    public Boolean getMethodSingleUseLock = false;
    private ArrayList<PubsubMessage> messageList = new ArrayList<PubsubMessage>();
    private ArrayList<Integer> fetchIdList = new ArrayList<Integer>();

    public void set(PubsubMessage message) {
        messageList.add(message);
    }

    public PubsubMessage get(Integer fetchClientId, Integer fetchClientTotal) throws Exception {
        // A dumb way to lock the method, but works well enough
        if (this.getMethodSingleUseLock == true) {
            throw new Exception("Locked for Single Use");
        }
        this.getMethodSingleUseLock = true;

        // Check if the specific ID was used for fetching and set it otherwise
        if (this.fetchIdList.contains(fetchClientId)) {
            this.getMethodSingleUseLock = false;
            throw new Exception("Already Fetched this Resource");
        }

        // Don't allow reading until all fetching clients have connected
        if (writeOnlyLock == true) {
            this.getMethodSingleUseLock = false;
            throw new Exception("Not Ready");
        }

        // Don't return anything if nothing to return
        if (messageList.size() == 0) {
            this.getMethodSingleUseLock = false;
            throw new Exception("Nothing Available");
        }

        // Indicate the fetch Id is used
        this.fetchIdList.add(fetchClientId);

        // Get the first message from the queue
        PubsubMessage message = messageList.get(0);

        // Remove the first message because it's been fetched by all parties
        if (this.fetchIdList.size() == fetchClientTotal) {
            messageList.remove(0);
            this.fetchIdList.clear();
        }

        // Unlock the method and return
        this.getMethodSingleUseLock = false;
        return message;
    }
}
