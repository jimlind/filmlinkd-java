package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.RefreshEmbedFactory;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefreshHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;
  @Autowired private RefreshEmbedFactory refreshEmbedFactory;

  public String getEventName() {
    return "refresh";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    if (accountMap == null) {
      // TODO: Log Error
      // TODO: Extract the error to another method
      event.getHook().sendMessage("Error").queue();
      return;
    }

    String userLID = this.memberWeb.getMemberLIDFromUsername(accountMap.getAsString());
    LBMember member = this.memberAPI.fetch(userLID);

    if (member == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    if (!this.firestoreManager.updateUserDisplayData(member)) {
      // TODO: Log something maybe
      // TODO: Extract failure messages to another method
      event.getHook().sendMessage("Refresh Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.refreshEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
