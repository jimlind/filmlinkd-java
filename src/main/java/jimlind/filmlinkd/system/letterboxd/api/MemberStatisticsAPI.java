package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.system.letterboxd.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberStatisticsAPI {
  @Autowired private Client client;

  public LBMemberStatistics fetch(String userLID) {
    if (userLID.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s/statistics", userLID);

    return this.client.getAuthorized(memberDetailsPath, LBMemberStatistics.class);
  }
}
