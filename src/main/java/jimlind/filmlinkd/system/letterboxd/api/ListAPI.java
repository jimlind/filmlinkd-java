package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.system.letterboxd.model.LBListsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListAPI {
  @Autowired private Client client;

  public LBListsResponse fetch(String userId, int count) {
    String uriTemplate = "lists/?member=%s&memberRelationship=%s&perPage=%s&where=%s";
    String path = String.format(uriTemplate, userId, "Owner", 50, "Published");

    LBListsResponse listsResponse = this.client.getAuthorized(path, LBListsResponse.class);
    if (listsResponse == null || listsResponse.items.isEmpty()) {
      return null;
    }

    return listsResponse;
  }
}
