package jimlind.filmlinkd.factory.messageEmbed;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedDescription;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedStars;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBReview;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class LoggedEmbedFactory {
  public ArrayList<MessageEmbed> create(List<LBLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    StringBuilder description = new StringBuilder();
    for (LBLogEntry logEntry : logEntryList) {
      String action = logEntry.review != null ? "Reviewed" : "Watched";
      String date = formatDate(logEntry.whenUpdated);
      description.append(
          String.format("[**%s on %s**](https://boxd.it/%s)\n", action, date, logEntry.id));

      String stars = new EmbedStars(logEntry.rating).build();
      String rewatch =
          logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch
              ? " <:r:851135667546488903>"
              : "";
      String like = logEntry.like ? " <:l:851138401557676073>" : "";
      description.append(stars).append(rewatch).append(like).append("\n");

      if (logEntry.review != null) {
        description.append(formatReview(logEntry.review)).append("\n");
      }
    }

    LBLogEntry firstLogEntry = logEntryList.get(0);
    String title =
        String.format(
            "%s's Recent Entries for %s (%s)\n",
            firstLogEntry.owner.displayName,
            firstLogEntry.film.name,
            firstLogEntry.film.releaseYear);
    embedBuilder.setTitle(title);
    embedBuilder.setThumbnail(new ImageHelper(firstLogEntry.film.poster).getTallest());
    embedBuilder.setDescription(new EmbedDescription(description.toString()).build());

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }

  private static String formatDate(String whenUpdated) {
    ZonedDateTime dateTime = ZonedDateTime.parse(whenUpdated);
    long timestamp = dateTime.toInstant().toEpochMilli();

    String pattern =
        Instant.now().toEpochMilli() - timestamp < 5000000000L ? "MMM dd" : "MMM dd uuu";

    return dateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
  }

  private static String formatReview(LBReview review) {
    String reviewText = review.text;
    if (reviewText.length() > 200) {
      reviewText = reviewText.substring(0, 200).trim();
    }
    Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    reviewText = new CopyDown(options).convert(reviewDocument.body().toString());
    if (review.text.length() > 200) {
      reviewText += "...";
    }

    reviewText = review.containsSpoilers ? "||" + reviewText + "||" : reviewText;
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");

    return reviewText;
  }
}
