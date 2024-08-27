package jimlind.filmlinkd.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.PubSub;

@Component
public class ShutdownThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(ShutdownThread.class);

    @Autowired
    PubSub pubSub;

    public void run() {
        this.logger.info("Shutting down...");
        this.pubSub.stop();
    }
}
