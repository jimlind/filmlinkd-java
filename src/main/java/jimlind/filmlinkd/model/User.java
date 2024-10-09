package jimlind.filmlinkd.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Map;

public class User {
  public String id;

  public Long checked;
  public Long created;
  public String displayName;
  public String image;
  public String letterboxdId;
  public String lid;
  public long updated;
  public String userName;

  public ArrayList<Channel> channelList;
  public Previous previous;
  public Footer footer;

  public static class Channel {
    public String channelId;
  }

  public static class Previous {
    public String id;
    public String lid;
    public ArrayList<String> list;
    public Long published;
    public String uri;
  }

  public static class Footer {
    public String icon;
    public String text;
  }

  public ArrayList<String> getChannelList() throws Exception {
    ArrayList<String> channelListResults = new ArrayList<String>();
    if (this.channelList == null) {
      return channelListResults;
    }

    for (Channel channel : this.channelList) {
      channelListResults.add(channel.channelId);
    }

    return channelListResults;
  }

  public String getMostRecentPrevious() {
    return previous.list.get(previous.list.size() - 1);
  }

  public Map<String, Object> toMap() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
  }
}
