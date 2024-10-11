package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class ContributorHandler implements Handler {
  public String getEventName() {
    return "contributor";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Nothing has been contributed.").queue();
  }
}
