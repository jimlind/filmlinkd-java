package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class FollowingHandler implements Handler {
  public String getEventName() {
    return "following";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("Lemmings aren't you?").queue();
  }
}
