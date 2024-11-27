package jimlind.filmlinkd.factory.messageEmbed;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedDescription;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBListEntrySummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
public class ListEmbedFactory {
  public ArrayList<MessageEmbed> create(LBListSummary listSummary) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    embedBuilder.setTitle(listSummary.name);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", listSummary.id));
    embedBuilder.setThumbnail(
        new ImageHelper(listSummary.previewEntries.get(0).film.poster).getTallest());

    StringBuilder descriptionText =
        new StringBuilder(
            String.format(
                "**List of %s films curated by [%s](https://boxd.it/%s)**\n\n",
                listSummary.filmCount, listSummary.owner.displayName, listSummary.owner.id));

    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String listDescription = new CopyDown(options).convert(listSummary.description);
    descriptionText.append(listDescription.replaceAll("[\r\n]+", "\n")).append("\n");

    for (LBListEntrySummary summary : listSummary.previewEntries) {
      String prefix = summary.rank != 0 ? "1." : "-";
      descriptionText.append(
          String.format(
              "%s [%s (%s)](https://boxd.it/%s)\n",
              prefix, summary.film.name, summary.film.releaseYear, summary.film.id));
    }

    String description = new EmbedDescription(descriptionText.toString()).build();
    embedBuilder.setDescription(description);

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
