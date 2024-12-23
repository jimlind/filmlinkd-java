package jimlind.filmlinkd.listener;

import jimlind.filmlinkd.system.discord.ProcessManager;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.thread.ShutdownThread;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
class AppListener implements ApplicationListener<ApplicationReadyEvent> {
  private @Autowired ProcessManager processManager;
  private @Autowired PubSubManager pubSubManager;
  private @Autowired ShutdownThread shutdownThread;

  @Override
  public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
    // Register the shutdownThread to the shutdownHook
    Runtime.getRuntime().addShutdownHook(this.shutdownThread);

    // Start the Discord App
    this.processManager.connect();

    // Activate the PubSub Publishers and Subscribers
    this.pubSubManager.activate();
  }
}
