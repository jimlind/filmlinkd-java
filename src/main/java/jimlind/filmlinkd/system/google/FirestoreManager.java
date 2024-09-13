package jimlind.filmlinkd.system.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.FirestoreOptions.Builder;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jimlind.filmlinkd.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirestoreManager {

    private final Config config;
    private final com.google.cloud.firestore.Firestore db;

    @Autowired
    public FirestoreManager(Config config) {
        this.config = config;

        Builder builder = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId(this.config.getGoogleProjectId());
        FirestoreOptions firestoreOptions = builder.build();
        this.db = firestoreOptions.getService();
    }

    public QueryDocumentSnapshot getUserDocument(String userLID) throws Exception {
        String collectionId = this.config.getFirestoreCollectionId();

        ApiFuture<QuerySnapshot> query = this.db.collection(collectionId).whereEqualTo("letterboxdId", userLID).limit(1).get();
        return query.get().getDocuments().get(0);
    }
}