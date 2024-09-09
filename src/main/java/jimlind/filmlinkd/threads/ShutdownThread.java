package jimlind.filmlinkd.threads;

import jimlind.filmlinkd.systems.discord.ProcessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.systems.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShutdownThread extends Thread {
    @Autowired
    private ProcessManager processManager;

    @Autowired
    private PubSubManager pubSubManager;

    public void run() {
        log.info("Shutting Things Down!");
        this.pubSubManager.stop();
        this.processManager.getShardManager().shutdown();
    }
}
