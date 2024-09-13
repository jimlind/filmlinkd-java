package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import jimlind.filmlinkd.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserFactory {

    public User createFromDocument(QueryDocumentSnapshot document) {
        try {
            return document.toObject(User.class);
        } catch (Exception e) {
            return null;
        }
    }
}
