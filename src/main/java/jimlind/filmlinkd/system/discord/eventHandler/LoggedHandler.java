package jimlind.filmlinkd.system.discord.eventHandler;

import jimlind.filmlinkd.factory.messageEmbed.LoggedEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoggedHandler implements Handler {
  @Autowired private FilmAPI filmAPI;
  @Autowired private LogEntriesAPI logEntriesAPI;
  @Autowired private LoggedEmbedFactory loggedEmbedFactory;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;

  public String getEventName() {
    return "logged";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    String accountAsString = accountMap != null ? accountMap.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(accountAsString);
    LBMember member = this.memberAPI.fetch(userLID);

    OptionMapping filmNameMap = event.getInteraction().getOption("film-name");
    String filmAsString = filmNameMap != null ? filmNameMap.getAsString() : "";
    LBFilmSummary film = this.filmAPI.search(filmAsString);

    if (member == null || film == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    List<LBLogEntry> logEntryList = this.logEntriesAPI.getByUserAndFilm(member.id, film.id);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.loggedEmbedFactory.create(logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
