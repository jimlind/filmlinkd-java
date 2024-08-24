package jimlind.filmlinkd;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.google.pubsub.v1.PubsubMessage;

@Component
// Qeue exists so that I can rate limit the amount of processing that happens.
// If we let every PubSub event trigger some logic it can take over the
// CPU really quickly.
public class Queue {
    public Boolean returnResults = false;
    private ArrayList<PubsubMessage> messageList = new ArrayList<PubsubMessage>();

    public void set(PubsubMessage message) {
        messageList.add(message);
    }

    public PubsubMessage get() throws Exception {
        if (returnResults == false) {
            throw new Exception("Not Ready");
        }

        if (messageList.size() == 0) {
            throw new Exception("Nothing Available");
        }

        PubsubMessage message = messageList.get(0);
        messageList.remove(0);

        return message;
    }
}
