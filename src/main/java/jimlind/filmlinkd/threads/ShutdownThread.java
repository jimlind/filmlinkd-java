package jimlind.filmlinkd.threads;

import jimlind.filmlinkd.systems.discord.DiscordProcessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.PubSub;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShutdownThread extends Thread {
    @Autowired
    private DiscordProcessManager discordProcessManager;

    @Autowired
    private PubSub pubSub;

    public void run() {
        log.info("Shutting Things Down!");
        this.pubSub.stop();
        this.discordProcessManager.getShardManager().shutdown();
    }
}