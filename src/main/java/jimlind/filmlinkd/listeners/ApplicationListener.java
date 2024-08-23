package jimlind.filmlinkd.listeners;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.PubSub;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

@Component
class MyApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    ConfigurableApplicationContext context = event.getApplicationContext();

    Config config = context.getBean(Config.class);
    DiscordListeners discordListeners = context.getBean(DiscordListeners.class);
    DefaultShardManagerBuilder.createLight(config.getDiscordBotToken()).addEventListeners(discordListeners).build();

    PubSub pubSub = context.getBean(PubSub.class);
    pubSub.run();
  }
}
