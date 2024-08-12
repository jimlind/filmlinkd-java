package jimlind.filmlinkd;

import net.dv8tion.jda.api.JDABuilder;

public class Main {
    private static final System.Logger LOGGER = System.getLogger("c.f.b.DefaultLogger");

    public static void main(String[] args) {
        LOGGER.log(System.Logger.Level.ERROR, "Logging A System Error");
        System.err.println("I am kind of lost now");
        System.out.println("jimlind.filmlinkd main:main");

        Config config = new Config();
        JDABuilder.createLight(config.getBotToken()).addEventListeners(new DiscordListeners()).build();
    }
}