package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class DiaryHandler implements Handler {
  public String getEventName() {
    return "diary";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Don't peak.").queue();
  }
}
