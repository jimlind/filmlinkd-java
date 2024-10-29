package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserFactory {

  public User createFromSnapshot(QueryDocumentSnapshot snapshot) {
    try {
      User user = snapshot.toObject(User.class);
      return this.fillDefaults(user);
    } catch (Exception e) {
      return null;
    }
  }

  public User createFromMember(LBMember member) {
    try {
      User user = new User();

      user.id = member.id;
      user.created = 0L; // TODO: Should this get filled?
      user.checked = 0L; // TODO: Should this get filled?
      user.displayName = member.displayName;
      user.image = new ImageHelper(member.avatar).getTallest();
      user.letterboxdId = member.id;
      user.updated = 0L; // TODO: Should this get filled?
      user.userName = member.username;

      return this.fillDefaults(user);
    } catch (Exception e) {
      return null;
    }
  }

  private User fillDefaults(User user) {
    // Fill the User.Previous with enough default data so other systems work as expected.
    // Maybe there's a better way to do this, but for now this seems reasonable
    if (user.previous == null) {
      user.previous = new User.Previous();
      user.previous.lid = "0";
      user.previous.list = new ArrayList<String>();
    }
    if (user.channelList == null) {
      user.channelList = new ArrayList<User.Channel>();
    }

    return user;
  }
}
