package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Handler {
  public String getEventName();

  public void handleEvent(SlashCommandInteractionEvent event);
}
