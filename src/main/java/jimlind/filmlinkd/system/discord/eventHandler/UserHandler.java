package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.UserEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.model.LBMemberStatistics;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserHandler implements Handler {
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberStatisticsAPI memberStatisticsAPI;
  @Autowired private MemberWeb memberWeb;
  @Autowired private UserEmbedFactory userEmbedFactory;

  public String getEventName() {
    return "user";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    String accountAsString = accountMap != null ? accountMap.getAsString() : "";

    String userLID = this.memberWeb.getMemberLIDFromUsername(accountAsString);
    LBMember member = this.memberAPI.fetch(userLID);
    LBMemberStatistics memberStatistics = this.memberStatisticsAPI.fetch(userLID);

    if (member == null || memberStatistics == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        this.userEmbedFactory.create(member, memberStatistics);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
