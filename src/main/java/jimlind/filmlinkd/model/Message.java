package jimlind.filmlinkd.model;

public class Message {
    public Entry entry;
    public String channelId;

    public static class Entry {
        public String lid;
        public String userName;
        public String userLid;
        public String type;
        public String link;
        public Long publishedDate;
        public String filmTitle;
        public Integer filmYear;
        public Long watchedDate;
        public String image;
        public Float starCount;
        public Boolean rewatch;
        public Boolean liked;
        public String containsSpoilers;
        public Boolean adult;
        public String review;
        public Long updatedDate;
        public String publishSource;
    }
}