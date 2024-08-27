package jimlind.filmlinkd;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.models.Message;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MessageUtility {
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
            log.info("Unable to fetch channel list from user", e);
        }

        return channelList;
    }
}
