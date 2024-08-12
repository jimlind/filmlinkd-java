package jimlind.filmlinkd;

import net.dv8tion.jda.api.JDABuilder;

public class Main {
    public static void main(String[] args) {
        System.err.println("I am kind of lost now");
        System.out.println("jimlind.filmlinkd main:main");

        Config config = new Config();
        JDABuilder.createLight(config.getBotToken()).addEventListeners(new DiscordListeners()).build();
    }
}