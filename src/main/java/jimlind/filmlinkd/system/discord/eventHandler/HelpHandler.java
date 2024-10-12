package jimlind.filmlinkd.system.discord.eventHandler;

import jimlind.filmlinkd.factory.messageEmbed.HelpEmbedFactory;
import jimlind.filmlinkd.system.google.FirestoreManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class HelpHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private HelpEmbedFactory helpEmbedFactory;

  public String getEventName() {
    return "help";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    String name = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();
    long userCount = this.firestoreManager.getUserCount();
    long guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();
    ArrayList<MessageEmbed> messageEmbedList =
        this.helpEmbedFactory.create(name, version, userCount, guildCount);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
