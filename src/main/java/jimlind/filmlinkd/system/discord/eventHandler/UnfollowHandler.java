package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
public class UnfollowHandler implements Handler {
  public String getEventName() {
    return "unfollow";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.reply("The opposite of follow.").queue();
  }
}
