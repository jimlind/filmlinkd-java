package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ContributorAPI {
  @Autowired private Client client;

  public LBSearchResponse fetch(String searchTerm) {
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s";
    String uri = String.format(uriTemplate, searchTerm, "ContributorSearchItem", 1);

    ResponseEntity<LBSearchResponse> response = this.client.get(uri, LBSearchResponse.class);
    if (response == null || response.getBody() == null || response.getBody().items.isEmpty()) {
      return null;
    }

    return response.getBody();
  }
}
