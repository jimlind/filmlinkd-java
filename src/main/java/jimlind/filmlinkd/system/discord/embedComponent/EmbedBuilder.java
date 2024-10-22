package jimlind.filmlinkd.system.discord.embedComponent;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;

public class EmbedBuilder extends net.dv8tion.jda.api.EmbedBuilder {
  @NotNull
  @Override
  public MessageEmbed build() {
    super.setColor(new Color(0xa700bd));
    return super.build();
  }
}
