package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.ContributorEmbedFactory;
import jimlind.filmlinkd.system.letterboxd.api.ContributorAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBContributor;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ContributorHandler implements Handler {
  @Autowired private ContributorAPI contributorAPI;
  @Autowired private ContributorEmbedFactory contributorEmbedFactory;

  public String getEventName() {
    return "contributor";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("contributor-name");
    if (optionMapping == null) {
      // TODO: Log Error
      // TODO: Extract the error to another method
      event.getHook().sendMessage("Error").queue();
      return;
    }

    LBSearchResponse searchResponse = this.contributorAPI.fetch(optionMapping.getAsString());
    if (searchResponse == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    LBContributor contributor = searchResponse.items.get(0).contributor;
    ArrayList<MessageEmbed> messageEmbedList = this.contributorEmbedFactory.create(contributor);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
