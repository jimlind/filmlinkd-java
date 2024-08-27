package jimlind.filmlinkd.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.PubSub;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShutdownThread extends Thread {

    @Autowired
    PubSub pubSub;

    public void run() {
        log.info("Shutting Things Down!");
        this.pubSub.stop();
    }
}
