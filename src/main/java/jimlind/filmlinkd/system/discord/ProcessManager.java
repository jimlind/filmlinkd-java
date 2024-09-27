package jimlind.filmlinkd.system.discord;

import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.listener.DiscordListener;
import lombok.Getter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessManager {
  @Autowired private Config config;
  @Autowired private DiscordListener discordListeners;
  @Getter private ShardManager shardManager;

  public void connect() {
    String token = config.getDiscordBotToken();
    this.shardManager =
        DefaultShardManagerBuilder.createLight(token).addEventListeners(discordListeners).build();
  }

  public void disconnect() {
    if (this.shardManager != null) {
      this.shardManager.shutdown();
    }
  }
}
