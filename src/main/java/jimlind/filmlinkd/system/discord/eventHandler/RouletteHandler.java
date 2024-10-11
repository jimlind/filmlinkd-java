package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class RouletteHandler implements Handler {
  public String getEventName() {
    return "roulette";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Gambler.").queue();
  }
}
