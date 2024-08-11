package jimlind.filmlinkd;

import net.dv8tion.jda.api.JDABuilder;

public class Main {
    public static void main(String[] args) {
        Config config = new Config();
        JDABuilder.createLight(config.getBotToken()).addEventListeners(new DiscordListeners()).build();
    }
}