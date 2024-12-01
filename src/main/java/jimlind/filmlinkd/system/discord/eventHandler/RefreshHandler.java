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

    OptionMapping optionMapping = event.getInteraction().getOption("account");
    String username = optionMapping != null ? optionMapping.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(username);
    LBMember member = this.memberAPI.fetch(userLID);

    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    if (!this.firestoreManager.updateUserDisplayData(member)) {
      event.getHook().sendMessage("Refresh Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.refreshEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
