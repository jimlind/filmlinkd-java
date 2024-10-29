package jimlind.filmlinkd.factory.messageEmbed;

import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedBuilder;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedDescription;
import jimlind.filmlinkd.system.discord.embedComponent.EmbedUser;
import jimlind.filmlinkd.system.letterboxd.ImageHelper;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

@Component
public class FollowEmbedFactory {
  public ArrayList<MessageEmbed> create(LBMember member) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    String userName = new EmbedUser(member.username).build();
    String description =
        String.format(
            "I am now following %s (%s).\nI'll try to post their most recent entry in the appropriate channel.",
            member.displayName, userName);
    embedBuilder.setDescription(new EmbedDescription(description).build());
    embedBuilder.setThumbnail(new ImageHelper(member.avatar).getTallest());

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
