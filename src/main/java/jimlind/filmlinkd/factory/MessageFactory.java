package jimlind.filmlinkd.factory;

import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.letterboxd.DateHelper;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.LinkHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;

public class MessageFactory {
  public Message createFromLogEntry(LBLogEntry logEntry, Message.PublishSource publishSource) {
    Message.Type type =
        (logEntry.review != null && !logEntry.review.text.isBlank())
            ? Message.Type.review
            : Message.Type.watch;
    String link = new LinkHelper(logEntry.links).getLetterboxd();

    Message message = new Message();

    message.entry = new Message.Entry();
    message.entry.lid = logEntry.id;
    message.entry.userName = logEntry.owner.username;
    message.entry.userLid = logEntry.owner.id;
    message.entry.type = type;
    message.entry.link = link;
    message.entry.publishedDate = new DateHelper(logEntry.whenCreated).getMilli();
    message.entry.filmTitle = logEntry.film.name;
    message.entry.filmYear = logEntry.film.releaseYear;
    message.entry.watchedDate =
        new DateHelper(logEntry.diaryDetails != null ? logEntry.diaryDetails.diaryDate : "")
            .getMilli();
    message.entry.image = new ImageHelper(logEntry.film.poster).getTallest();
    message.entry.starCount = logEntry.rating;
    message.entry.rewatch = logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch;
    message.entry.liked = logEntry.like;
    message.entry.containsSpoilers = logEntry.review != null && logEntry.review.containsSpoilers;
    message.entry.adult = logEntry.film.adult;
    message.entry.review = logEntry.review != null ? logEntry.review.text : "";
    message.entry.updatedDate = new DateHelper(logEntry.whenUpdated).getMilli();
    message.entry.publishSource = publishSource;

    return message;
  }
}
