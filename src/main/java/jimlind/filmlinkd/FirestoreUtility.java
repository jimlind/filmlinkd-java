package jimlind.filmlinkd;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.FirestoreOptions.Builder;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import jimlind.filmlinkd.models.User;
import jimlind.filmlinkd.models.User.Channel;

@Component
public class FirestoreUtility {
    private static final Logger logger = LoggerFactory.getLogger(FirestoreUtility.class);

    private final Config config;
    private final Firestore db;

    @Autowired
    public FirestoreUtility(Config config) {
        this.config = config;

        Builder builder = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(this.config.getGoogleProjectId());
        FirestoreOptions firestoreOptions = builder.build();
        this.db = firestoreOptions.getService();
    }

    public ArrayList<String> getUserChannelList(String userLID) throws Exception {
        ArrayList<String> channelListResults = new ArrayList<String>();
        System.out.println(userLID);

        ApiFuture<QuerySnapshot> query = this.db.collection("users-dev").whereEqualTo("letterboxdId", userLID).get();
        QuerySnapshot querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
        QueryDocumentSnapshot document = documents.get(0);
        User user = new User();

        try {
            user = document.toObject(User.class);
        } catch (Exception e) {
            logger.error("Unable to Cast User via LetterboxdID [" + userLID + "]");
            return channelListResults;
        }

        for (Channel channel : user.channelList) {
            channelListResults.add(channel.channelId);
        }

        return channelListResults;
    }
}