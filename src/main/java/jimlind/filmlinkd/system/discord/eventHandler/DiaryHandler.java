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

    OptionMapping optionMapping = event.getInteraction().getOption("account");
    String username = optionMapping != null ? optionMapping.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(username);
    LBMember member = this.memberAPI.fetch(userLID);
    List<LBLogEntry> logEntryList = this.logEntriesAPI.getRecentForUser(userLID, 5);

    if (member == null || logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        this.diaryListEmbedFactory.create(member, logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
