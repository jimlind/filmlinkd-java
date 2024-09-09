package jimlind.filmlinkd.systems.discord;

import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.listeners.DiscordListeners;
import lombok.Getter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscordProcessManager {
    @Autowired
    private Config config;

    @Autowired
    private DiscordListeners discordListeners;

    @Getter
    private ShardManager shardManager;

    public void connect() {
        String token = config.getDiscordBotToken();

        // TODO: Remove the 3 total shards, that's just a good way for me to test it is doing things
        this.shardManager = DefaultShardManagerBuilder.createLight(token).setShardsTotal(3).addEventListeners(discordListeners).build();
    }

}
