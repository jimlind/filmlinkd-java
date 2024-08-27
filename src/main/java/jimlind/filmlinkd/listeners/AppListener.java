package jimlind.filmlinkd.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jimlind.filmlinkd.Config;
import jimlind.filmlinkd.PubSub;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

@Component
class AppListener implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired
  Config config;

  @Autowired
  DiscordListeners discordListeners;

  @Autowired
  PubSub pubSub;

  @Autowired
  ShutdownThread shutdownThread;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(this.shutdownThread);

    // Start the Discord App
    // TODO: Remove the 4 total shards, that's just a good way for me to test it
    // because it wires up different shards correctly.
    DefaultShardManagerBuilder.createLight(this.config.getDiscordBotToken()).setShardsTotal(4)
        .addEventListeners(this.discordListeners).build();

    // Start the PubSub listener
    this.pubSub.start();
  }
}
