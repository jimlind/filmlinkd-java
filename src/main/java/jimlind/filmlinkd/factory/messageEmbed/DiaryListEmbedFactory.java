package jimlind.filmlinkd.factory.messageEmbed;

import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedStars;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedUser;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DiaryListEmbedFactory {
  public ArrayList<MessageEmbed> create(LBMember member, List<LBLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    ArrayList<String> entryList = new ArrayList<>();
    for (LBLogEntry logEntry : logEntryList) {
      String firstLine =
          String.format(
              "[**%s (%s)**](https://boxd.it/%s)",
              logEntry.film.name, logEntry.film.releaseYear, logEntry.id);

      String secondLine = logEntry.diaryDetails != null ? logEntry.diaryDetails.diaryDate : "";
      secondLine += " " + new EmbedStars(logEntry.rating).build();
      secondLine +=
          logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch
              ? " <:r:851135667546488903>"
              : "";
      secondLine += logEntry.like ? " <:l:851138401557676073>" : "";
      secondLine += logEntry.review != null ? " :speech_balloon:" : "";

      entryList.add(firstLine + "\n" + secondLine);
    }

    String out = String.join("\n", entryList);
    embedBuilder.setDescription(out);

    embedBuilder.setTitle(
        String.format("Recent Diary Activity from %s", new EmbedUser(member.displayName).build()));
    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(new ImageHelper(member.avatar).getTallest());

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
