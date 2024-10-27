package jimlind.filmlinkd.factory.messageEmbed;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.discord.embedComponent.*;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBFilm;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
public class FilmEmbedFactory {
  public ArrayList<MessageEmbed> create(CombinedLBFilmModel filmCombination) {
    LBFilm film = filmCombination.film;
    LBFilmStatistics statistics = filmCombination.filmStatistics;
    LBFilmSummary summary = filmCombination.filmSummary;

    String releaseYear = film.releaseYear > 0 ? String.format(" (%s)", film.releaseYear) : "";
    String imageURL = new ImageHelper(film.poster).getTallest();

    EmbedBuilder embedBuilder = new EmbedBuilder();
    embedBuilder.setTitle(film.name + releaseYear);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", film.id));
    embedBuilder.setThumbnail(imageURL.isBlank() ? null : imageURL);

    String description = "";
    // Add tagline to description
    if (film.tagline != null) {
      description += String.format("**%s**\n", film.tagline);
    }

    // Add rating to description
    if (summary.rating > 0) {
      String stars = new EmbedStars(summary.rating).build();
      String rating = String.format("%.2f", summary.rating);
      description += stars + " " + rating + "\n";
    }

    // Add directors to description
    String directors = new EmbedDirectors(film.contributions).build();
    if (!directors.isBlank()) {
      description += directors + "\n";
    }

    // Add primary language and runtime
    List<String> metadata = new ArrayList<>();
    if (film.primaryLanguage != null) {
      metadata.add(film.primaryLanguage.name);
    }
    if (film.runTime > 0) {
      metadata.add(new EmbedRunTime(film.runTime).build());
    }
    if (!metadata.isEmpty()) {
      description += String.join(", ", metadata) + "\n";
    }

    // Add genres
    if (!film.genres.isEmpty()) {
      String genres = film.genres.stream().map(g -> g.name).collect(Collectors.joining(", "));
      description += genres + "\n";
    }

    // Add statistics counts
    LBFilmStatisticsCounts counts = statistics.counts;
    description += ":eyes: " + new EmbedCount(counts.watches).build() + ", ";
    description += "<:r:851138401557676073> " + new EmbedCount(counts.likes).build() + ", ";
    description += ":speech_balloon: " + new EmbedCount(counts.reviews).build() + "\n";

    // Build it
    embedBuilder.setDescription(new EmbedDescription(description).build());
    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
