package jimlind.filmlinkd.system.discord.eventHandler;

import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilmHandler implements Handler {
  @Autowired private FilmAPI filmAPI;
  @Autowired private FilmEmbedFactory filmEmbedFactory;

  public String getEventName() {
    return "film";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    if (optionMapping == null) {
      // TODO: Log Error
      // TODO: Extract the error to another method
      event.getHook().sendMessage("Error").queue();
      return;
    }

    CombinedLBFilmModel combinedLBFilmModel = this.filmAPI.fetch(optionMapping.getAsString());
    if (combinedLBFilmModel == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.filmEmbedFactory.create(combinedLBFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
