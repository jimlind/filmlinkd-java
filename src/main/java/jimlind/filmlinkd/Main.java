package jimlind.filmlinkd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDABuilder;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting Things Up!");

        Config config = new Config();
        Queue queue = new Queue();
        JDABuilder.createLight(config.getBotToken()).addEventListeners(new DiscordListeners(queue)).build();

        PubSub pubSub = new PubSub(config, queue);
    }
}