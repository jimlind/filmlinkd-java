package jimlind.filmlinkd.system.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.FirestoreOptions.Builder;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.Instant;
import java.util.ArrayList;
import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirestoreManager {

  private final Config config;
  private final UserFactory userFactory;
  private final com.google.cloud.firestore.Firestore db;

  @Autowired
  public FirestoreManager(Config config, UserFactory userFactory) {
    this.config = config;
    this.userFactory = userFactory;

    Builder builder =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(this.config.getGoogleProjectId());
    FirestoreOptions firestoreOptions = builder.build();
    this.db = firestoreOptions.getService();
  }

  public QueryDocumentSnapshot getUserDocument(String userLID) throws Exception {
    String collectionId = this.config.getFirestoreCollectionId();

    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereEqualTo("letterboxdId", userLID).limit(1).get();
    return query.get().getDocuments().get(0);
  }

  public boolean updateUserPrevious(
      String userLID, String diaryLID, long diaryPublishedDate, String diaryURI) {
    try {
      QueryDocumentSnapshot snapshot = getUserDocument(userLID);
      User user = this.userFactory.createFromSnapshot(snapshot);
      ArrayList<String> previousList = user.previous.list;

      if (LidComparer.compare(user.previous.lid, diaryLID) < 0) {
        // This is the important data to update
        // The scraping process uses it to determine which entries are new
        user.previous.lid = diaryLID;
        // This data is only used if I'm trying to debug publishing issues
        user.updated = Instant.now().toEpochMilli();
        user.previous.published = diaryPublishedDate;
        user.previous.uri = diaryURI;
        // This list is not a source of truth
        // It will only contain LIDs that are the most recent when published
        user.previous.list = LidComparer.buildMostRecentList(previousList, diaryLID, 10);
      }

      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.error("Unable to update user", e);
      return false;
    }

    return true;
  }
}
