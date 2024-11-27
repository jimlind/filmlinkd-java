package jimlind.filmlinkd.model;

import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;

public class Message {
  public enum Type {
    watch,
    review,
  };

  public enum PublishSource {
    Normal,
    VIP,
    Follow,
  }

  public Entry entry;
  @Nullable public String channelId;

  public static class Entry {
    public String lid;
    public String userName;
    public String userLid;
    public Message.Type type;
    public String link;
    public long publishedDate;
    public String filmTitle;
    public Integer filmYear;
    public long watchedDate;
    public String image;
    public float starCount;
    public Boolean rewatch;
    public Boolean liked;
    public Boolean containsSpoilers;
    public Boolean adult;
    public String review;
    public long updatedDate;
    public Message.PublishSource publishSource;
  }

  public boolean hasChannelOverride() {
    if (this.channelId == null) {
      return false;
    }

    return !this.channelId.isBlank();
  }

  public String toJson() {
    return new Gson().toJson(this);
  }
}
