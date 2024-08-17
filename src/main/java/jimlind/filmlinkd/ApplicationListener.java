package jimlind.filmlinkd;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import net.dv8tion.jda.api.JDABuilder;

@Component
class MyApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    ConfigurableApplicationContext context = event.getApplicationContext();

    Config config = context.getBean(Config.class);
    DiscordListeners discordListeners = context.getBean(DiscordListeners.class);
    JDABuilder.createLight(config.getBotToken()).addEventListeners(discordListeners).build();
  }
}
