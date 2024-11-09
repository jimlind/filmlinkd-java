package jimlind.filmlinkd.system.letterboxd;

import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LBLink;
import jimlind.filmlinkd.system.letterboxd.model.LBLinkType;

public class LinkHelper {
  private final List<LBLink> linkList;

  public LinkHelper(List<LBLink> linkList) {
    this.linkList = linkList;
  }

  public String getLetterboxd() {
    if (this.linkList.isEmpty()) {
      return "";
    }

    for (LBLink link : this.linkList) {
      if (link.type == LBLinkType.letterboxd) {
        return link.url;
      }
    }

    return "";
  }
}
