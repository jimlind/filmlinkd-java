package jimlind.filmlinkd;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.models.Message;

@Component
public class MessageUtility {
    private static final Logger logger = LoggerFactory.getLogger(PubSub.class);
    private final FirestoreUtility firestoreUtility;

    @Autowired
    public MessageUtility(FirestoreUtility firestoreUtility) {
        this.firestoreUtility = firestoreUtility;
    }

    public ArrayList<String> getChannelList(Message message) {
        ArrayList<String> channelList = new ArrayList<String>();
        if (!message.channelId.isBlank()) {
            channelList.add(message.channelId);
            return channelList;
        }

        try {
            return this.firestoreUtility.getUserChannelList(message.entry.userLid);
        } catch (Exception e) {
            logger.info("Unable to fetch channel list from user", e);
        }

        return channelList;
    }
}
