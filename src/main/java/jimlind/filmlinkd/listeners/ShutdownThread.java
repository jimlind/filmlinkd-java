package jimlind.filmlinkd.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.PubSub;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;

@Component
@Slf4j
public class ShutdownThread extends Thread {
    private JDA jda;

    @Autowired
    private PubSub pubSub;

    public void setJda(JDA jda) {
        this.jda = jda;
    }

    public void run() {
        log.info("Shutting Things Down!");
        this.pubSub.stop();
        if (this.jda != null) {
            this.jda.shutdown();
        }
    }
}
