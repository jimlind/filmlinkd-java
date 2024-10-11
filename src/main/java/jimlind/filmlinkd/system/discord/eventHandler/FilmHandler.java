package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class FilmHandler implements Handler {
  public String getEventName() {
    return "film";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("It is a movie.").queue();
  }
}
