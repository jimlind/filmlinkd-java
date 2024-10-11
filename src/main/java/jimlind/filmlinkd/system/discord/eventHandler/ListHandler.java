package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class ListHandler implements Handler {
  public String getEventName() {
    return "list";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Santa is coming to town.").queue();
  }
}
