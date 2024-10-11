package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class FollowHandler implements Handler {
  public String getEventName() {
    return "follow";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Join the pack.").queue();
  }
}
