package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class LoggedHandler implements Handler {
  public String getEventName() {
    return "logged";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Big. Heavy. Wood.").queue();
  }
}