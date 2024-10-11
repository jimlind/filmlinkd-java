package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class HelpHandler implements Handler {
  public String getEventName() {
    return "help";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("You are beyond help.").queue();
  }
}
