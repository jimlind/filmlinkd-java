package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.system.letterboxd.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberAPI {
  @Autowired private Client client;

  public LBMember fetch(String userLID) {
    String memberDetailsPath = String.format("member/%s", userLID);
    ResponseEntity<LBMember> memberDetailsResponse =
        this.client.getAuthorized(memberDetailsPath, LBMember.class);
    if (memberDetailsResponse == null || memberDetailsResponse.getBody() == null) {
      return null;
    }

    return memberDetailsResponse.getBody();
  }
}
