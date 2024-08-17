package jimlind.filmlinkd;

import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.google.pubsub.v1.PubsubMessage;

@Component
public class Queue {
    ArrayList<PubsubMessage> messageList = new ArrayList<PubsubMessage>();

    public void set(PubsubMessage message) {
        messageList.add(message);
    }

    public PubsubMessage get() throws Exception {
        if (messageList.size() == 0) {
            throw new Exception("Nothing Available");
        }

        PubsubMessage message = messageList.get(0);
        messageList.remove(0);

        return message;
    }
}
