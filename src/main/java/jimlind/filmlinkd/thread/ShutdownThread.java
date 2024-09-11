package jimlind.filmlinkd.thread;

import jimlind.filmlinkd.system.discord.ProcessManager;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ShutdownThread extends Thread {
    @Autowired
    private ProcessManager discordProcessManager;

    @Autowired
    private PubSubManager pubSubManager;

    public void run() {
        log.info("Shutting Things Down!");
        this.pubSubManager.stop();
        this.discordProcessManager.disconnect();
    }
}
