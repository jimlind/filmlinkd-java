package jimlind.filmlinkd.system.letterboxd.api;

import java.net.URI;
import java.util.function.Function;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;

@Component
public class ContributorAPI {
  @Autowired private Client client;

  public LBSearchResponse fetch(String searchTerm) {
    Function<UriBuilder, URI> buildURI =
        (UriBuilder uriBuilder) ->
            uriBuilder
                .path("/search/")
                .queryParam("input", searchTerm)
                .queryParam("include", "ContributorSearchItem")
                .queryParam("perPage", 1)
                .build();

    ResponseEntity<LBSearchResponse> response = this.client.get(buildURI, LBSearchResponse.class);
    if (response == null || response.getBody() == null || response.getBody().items.isEmpty()) {
      return null;
    }

    return response.getBody();
  }
}
