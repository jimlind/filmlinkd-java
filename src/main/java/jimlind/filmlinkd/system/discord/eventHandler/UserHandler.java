package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class UserHandler implements Handler {
  public String getEventName() {
    return "user";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply(":construction: UNDER CONSTRUCTION :construction:").queue();
  }
}
