package jimlind.filmlinkd.model;

import com.google.gson.Gson;

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
  public String channelId;

  public static class Entry {
    public String lid;
    public String userName;
    public String userLid;
    public Message.Type type;
    public String link;
    public Long publishedDate;
    public String filmTitle;
    public Integer filmYear;
    public Long watchedDate;
    public String image;
    public Float starCount;
    public Boolean rewatch;
    public Boolean liked;
    public Boolean containsSpoilers;
    public Boolean adult;
    public String review;
    public Long updatedDate;
    public Message.PublishSource publishSource;
  }

  public boolean hasChannelOverride() {
    return !this.channelId.isBlank();
  }

  public String toJson() {
    return new Gson().toJson(this);
  }
}
