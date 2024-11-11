package jimlind.filmlinkd.system.letterboxd.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/LogEntry
public class LBLogEntry {
  public String id;
  public String name;
  public LBMemberSummary owner;
  public LBFilmSummary film;
  public LBDiaryDetails diaryDetails;
  @Nullable public LBReview review;
  @Deprecated public List<String> tags;
  public List<LBTag> tags2;
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
  @FirstParty public List<String> targeting;
}
