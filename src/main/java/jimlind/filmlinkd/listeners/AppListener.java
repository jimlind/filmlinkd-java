package jimlind.filmlinkd.listeners;

import jimlind.filmlinkd.PubSub;
import jimlind.filmlinkd.systems.discord.DiscordProcessManager;
import jimlind.filmlinkd.threads.ShutdownThread;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
class AppListener implements ApplicationListener<ApplicationReadyEvent> {

  @Autowired
  DiscordProcessManager discordProcessManager;

  @Autowired
  PubSub pubSub;

  @Autowired
  ShutdownThread shutdownThread;

  @Override
  public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(this.shutdownThread);

    // Start the Discord App
    this.discordProcessManager.connect();

    // Start the PubSub listener
    this.pubSub.start();
  }
}
