package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import jimlind.filmlinkd.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserFactory {

  public User createFromSnapshot(QueryDocumentSnapshot snapshot) {
    try {
      User user = snapshot.toObject(User.class);
      // Fill the User.Previous with enough default data so other systems work as expected.
      // Maybe there's a better way to do this, but for now this seems reasonable
      if (user.previous == null) {
        user.previous = new User.Previous();
        user.previous.lid = "0";
        user.previous.list = new ArrayList<String>();
      }
      return user;
    } catch (Exception e) {
      return null;
    }
  }
}
