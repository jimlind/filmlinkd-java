package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/LogEntry
public class LBLogEntry {
  public String id;
  public String name;
  public LBMemberSummary owner;
  public LBFilmSummary film;
  public LBDiaryDetails diaryDetails;
  public LBReview review;
  @Deprecated public List<String> tags;
  public List<String> tags2;
  public String whenCreated;
  public String whenUpdated;
  public float rating;
  public boolean like;
  public boolean commentable;
  // commentPolicy - CommentPolicy
  public List<LBLink> links;
  @FirstParty public String posterPickerUrl;
  public LBImage backdrop;
  public float backdropFocalPoint;
  public List<String> targeting;
}
