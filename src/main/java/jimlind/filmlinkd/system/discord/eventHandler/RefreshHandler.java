package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class RefreshHandler implements Handler {
  public String getEventName() {
    return "refresh";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Redux.").queue();
  }
}
