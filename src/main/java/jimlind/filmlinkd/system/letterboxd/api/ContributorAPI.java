package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

@Component
public class ContributorAPI {
  @Autowired private Client client;

  public LBSearchResponse fetch(String searchTerm) {
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s";
    String input = UriUtils.encodePath(searchTerm, "UTF-8");
    String path = String.format(uriTemplate, input, "ContributorSearchItem", 1);

    LBSearchResponse searchResponse = this.client.get(path, LBSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    return searchResponse;
  }
}
