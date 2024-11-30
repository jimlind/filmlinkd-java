package jimlind.filmlinkd.factory.messageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedDescription;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedText;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedUser;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.*;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
public class UserEmbedFactory {
  public ArrayList<MessageEmbed> create(LBMember member, LBMemberStatistics memberStatistics) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    String displayName = new EmbedUser(member.displayName).build();
    LBPronoun pronoun = member.pronoun;
    List<String> pronounList =
        List.of(pronoun.subjectPronoun, pronoun.objectPronoun, pronoun.possessivePronoun);

    String description = "";
    if (member.location != null) {
      description += String.format("***%s***\n", member.location);
    }

    if (!member.bio.isBlank()) {
      description += new EmbedText(member.bio).build(1000);
      description += "\n------------\n";
    }

    Function<LBFilmSummary, String> mapFilmToString =
        film ->
            String.format("- [%s (%s)](https://boxd.it/%s)", film.name, film.releaseYear, film.id);
    List<String> filmStringList = member.favoriteFilms.stream().map(mapFilmToString).toList();
    description += String.join("\n", filmStringList) + "\n";

    description +=
        String.format(
            "Logged films: %s total | %s this year",
            memberStatistics.counts.watches, memberStatistics.counts.filmsInDiaryThisYear);

    embedBuilder.setTitle(displayName + " " + String.join("/", pronounList));
    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(new ImageHelper(member.avatar).getTallest());
    embedBuilder.setDescription(new EmbedDescription(description).build());

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
