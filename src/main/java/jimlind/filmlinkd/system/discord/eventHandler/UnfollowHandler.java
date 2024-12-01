package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.UnfollowEmbedFactory;
import jimlind.filmlinkd.system.discord.ChannelHelper;
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
public class UnfollowHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;
  @Autowired private UnfollowEmbedFactory unfollowEmbedFactory;

  public String getEventName() {
    return "unfollow";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.toString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(userName);
    LBMember member = this.memberAPI.fetch(userLID);

    if (member == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("Channel Not Found").queue();
      return;
    }

    if (!this.firestoreManager.removeUserSubscription(member.id, channelId)) {
      // TODO: Log something maybe
      // TODO: Extract failure messages to another method
      event.getHook().sendMessage("Unfollow Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.unfollowEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
