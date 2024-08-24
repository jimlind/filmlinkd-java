package jimlind.filmlinkd.models;

import java.util.ArrayList;

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
}