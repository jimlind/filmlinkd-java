package jimlind.filmlinkd.system.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.firestore.FirestoreOptions.Builder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
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

  public void createUserDocument(LBMember member) {
    String collectionId = this.config.getFirestoreCollectionId();
    User user = this.userFactory.createFromMember(member);

    try {
      this.db.collection(collectionId).document(user.id).set(user.toMap()).get();
    } catch (Exception e) {
      log.atError().setMessage("Unable to set user document").log();
    }
  }

  public QueryDocumentSnapshot getUserDocument(String userLID) {
    String collectionId = this.config.getFirestoreCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereEqualTo("letterboxdId", userLID).limit(1).get();

    try {
      return query.get().getDocuments().get(0);
    } catch (Exception e) {
      return null;
    }
  }

  public long getUserCount() {
    String collectionId = this.config.getFirestoreCollectionId();
    ApiFuture<AggregateQuerySnapshot> query = this.db.collection(collectionId).count().get();
    try {
      return query.get().getCount();
    } catch (Exception e) {
      return 0;
    }
  }

  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    String collectionId = this.config.getFirestoreCollectionId();
    Map<String, String> channelMap = Map.ofEntries(Map.entry("channelId", channelId));
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereArrayContains("channelList", channelMap).get();
    try {
      return query.get().getDocuments();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public boolean addUserSubscription(String userLID, String channelId) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.loadDocumentSnapshotByUserLID(userLID);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      System.out.println("user doesn't exist");
      return false;
    }

    ArrayList<User.Channel> channelList = user.channelList;

    // If the channel is already subscribed then exit early positively
    for (User.Channel channel : channelList) {
      if (channel.channelId.equals(channelId)) {
        return true;
      }
    }

    User.Channel newChannel = new User.Channel();
    newChannel.channelId = channelId;
    user.channelList.add(newChannel);

    // Perform the update
    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      System.out.println(e);
      log.atError()
          .setMessage("Unable to Update Channel List: Update Failed")
          .addKeyValue("user", user)
          .addKeyValue("channelId", channelId)
          .log();
      return false;
    }

    return true;
  }

  public boolean removeUserSubscription(String userLID, String channelId) {
    // TODO: Fill this out
    return false;
  }

  public boolean updateUserPrevious(
      String userLID, String diaryLID, long diaryPublishedDate, String diaryURI) {

    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.loadDocumentSnapshotByUserLID(userLID);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    ArrayList<String> previousList = user.previous.list;

    // Nothing to update. Return `true` as if the action succeeded.
    if (previousList.contains(diaryLID)) {
      return true;
    }

    // The previous list should be considered the primary source of truth
    user.previous.list = LidComparer.buildMostRecentList(previousList, diaryLID, 10);

    // The scraping process uses the previous lid as it's source of truth to determine which
    // entries are new. Maybe that should be changed eventually.
    user.previous.lid = user.getMostRecentPrevious();

    // This data is only used if I'm trying to debug publishing issues
    user.updated = Instant.now().toEpochMilli();
    user.previous.published = diaryPublishedDate;
    user.previous.uri = diaryURI;

    // Perform the update
    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to Update Previous: Update Failed")
          .addKeyValue("user", user)
          .addKeyValue("diaryLID", diaryLID)
          .log();
      return false;
    }

    return true;
  }

  private QueryDocumentSnapshot loadDocumentSnapshotByUserLID(String userLID) {
    QueryDocumentSnapshot snapshot = getUserDocument(userLID);
    if (snapshot == null) {
      log.atWarn()
          .setMessage("Unable to Update Previous: User Not Found")
          .addKeyValue("userLID", userLID)
          .log();
      return null;
    }

    return snapshot;
  }
}
