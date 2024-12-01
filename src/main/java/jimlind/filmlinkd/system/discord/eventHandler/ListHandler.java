package jimlind.filmlinkd.system.discord.eventHandler;

import jimlind.filmlinkd.factory.messageEmbed.ListEmbedFactory;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.api.ListAPI;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBListSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBListsResponse;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ListHandler implements Handler {
  @Autowired private ListAPI listAPI;
  @Autowired private ListEmbedFactory listEmbedFactory;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;
  @Autowired private PubSubManager pubSubManager;

  public String getEventName() {
    return "list";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    String username = accountMap != null ? accountMap.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(username);
    LBMember member = this.memberAPI.fetch(userLID);

    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    OptionMapping listOptionMapping = event.getInteraction().getOption("list-name");
    String listNameString = listOptionMapping != null ? listOptionMapping.getAsString() : "";

    String cleanListName = listNameString.toLowerCase().replaceAll("[^a-z0-9]+", "");

    LBListSummary foundList = null;
    LBListsResponse listsResponse = this.listAPI.fetch(userLID, 50);
    if (listsResponse == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    for (LBListSummary item : listsResponse.items) {
      if (cleanListName.equals(item.name.toLowerCase().replaceAll("[^a-z0-9]+", ""))) {
        foundList = item;
      }
    }

    if (foundList == null) {
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.listEmbedFactory.create(foundList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
