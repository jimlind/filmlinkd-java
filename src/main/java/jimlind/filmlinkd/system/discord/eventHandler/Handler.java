package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Handler {
  public String NO_RESULTS_FOUND = "No Results Found";
  public String NO_CHANNEL_FOUND = "No Channel Found";

  public String getEventName();

  public void handleEvent(SlashCommandInteractionEvent event);
}
