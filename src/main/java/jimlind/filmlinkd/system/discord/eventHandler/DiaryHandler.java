package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.messageEmbed.DiaryListEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiaryHandler implements Handler {
  @Autowired private DiaryListEmbedFactory diaryListEmbedFactory;
  @Autowired private LogEntriesAPI logEntriesAPI;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;

  public String getEventName() {
    return "diary";
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

    List<LBLogEntry> logEntryList = this.logEntriesAPI.getRecentForUser(userLID, 5);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        this.diaryListEmbedFactory.create(member, logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
