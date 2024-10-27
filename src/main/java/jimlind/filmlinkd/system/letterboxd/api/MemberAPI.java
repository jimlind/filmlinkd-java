package jimlind.filmlinkd.system.letterboxd.api;

import jimlind.filmlinkd.model.CombinedLBMemberModel;
import jimlind.filmlinkd.system.letterboxd.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberAPI {
  @Autowired private Client client;

  public CombinedLBMemberModel fetch(String username) {
    // Search for the member by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s";
    String searchPath = String.format(uriTemplate, username, "MemberSearchItem", 4);
    ResponseEntity<LBSearchResponse> searchResponse =
        this.client.get(searchPath, LBSearchResponse.class);

    // Exit if no worthwhile responses from the client
    if (searchResponse == null
        || searchResponse.getBody() == null
        || searchResponse.getBody().items.isEmpty()) {
      return null;
    }

    // Ensure that search results are correct
    LBMemberSummary memberSummary = null;
    for (LBAbstractSearchItem searchItem : searchResponse.getBody().items) {
      if (username.equals(searchItem.member.username)) {
        memberSummary = searchItem.member;
      }
      break;
    }

    // Exit if no matching users in the search
    if (memberSummary == null) {
      return null;
    }

    // Load member details
    String memberDetailsPath = String.format("member/%s", memberSummary.id);
    ResponseEntity<LBMember> memberDetailsResponse =
        this.client.getAuthorized(memberDetailsPath, LBMember.class);
    if (memberDetailsResponse == null || memberDetailsResponse.getBody() == null) {
      return null;
    }

    CombinedLBMemberModel combinedLBMemberModel = new CombinedLBMemberModel();
    combinedLBMemberModel.member = memberDetailsResponse.getBody();
    combinedLBMemberModel.memberSummary = memberSummary;

    return combinedLBMemberModel;
  }
}
