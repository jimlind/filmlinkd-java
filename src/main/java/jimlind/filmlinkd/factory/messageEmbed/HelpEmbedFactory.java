package jimlind.filmlinkd.factory.messageEmbed;

import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedDescription;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
public class HelpEmbedFactory {
  public ArrayList<MessageEmbed> create(
      String name, String version, long userCount, long guildCount) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    // Set title
    embedBuilder.setTitle("(Help!) I Need Somebody", "https://jimlind.github.io/filmlinkd/");

    // Set description
    String descriptionText =
        String.format(
            "%s v%s\nTracking %s users on %s servers", name, version, userCount, guildCount);
    embedBuilder.setDescription(new EmbedDescription(descriptionText).build());

    // Set fields for slash commands
    embedBuilder.addField("/help", "Shows this message", false);
    embedBuilder.addField("/follow account [channel]", "Start listening for new entries", false);
    embedBuilder.addField("/unfollow account [channel]", "Stops listening for new entries", false);
    embedBuilder.addField("/following", "List all users followed in this channel", false);
    embedBuilder.addField(
        "/refresh account", "Refreshes the Filmlinkd cache for the account", false);
    embedBuilder.addField(
        "/contributor contributor-name", "Shows a film contributor's information", false);
    embedBuilder.addField("/diary account", "Shows a user's 5 most recent entries", false);
    embedBuilder.addField("/film film-name", "Shows a film's information", false);
    embedBuilder.addField("/list account list-name", "Shows a user's list summary", false);
    embedBuilder.addField("/logged account film-name", "Shows a user's entries for a film", false);
    embedBuilder.addField("/roulette", "Shows random film information", false);
    embedBuilder.addField("/user account", "Shows a users's information", false);

    // Set fields for support links
    embedBuilder.addField(
        ":clap: Patreon", "[Support on Patreon](https://www.patreon.com/filmlinkd)", true);
    embedBuilder.addField(
        ":coffee: Ko-fi", "[Support on Ko-fi](https://ko-fi.com/filmlinkd)", true);
    embedBuilder.addField(
        ":left_speech_bubble: Discord", "[Join the Discord](https://discord.gg/deZ7EUguge)", true);

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
