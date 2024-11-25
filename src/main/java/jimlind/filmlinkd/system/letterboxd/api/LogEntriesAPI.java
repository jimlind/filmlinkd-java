package jimlind.filmlinkd.system.letterboxd.api;

import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LogEntriesAPI {
  @Autowired private Client client;

  public List<LBLogEntry> getRecentForUser(String userId, int count) {
    String uriTemplate = "log-entries/?member=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, "Owner", count);

    LBLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LBLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.items.isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }
}
