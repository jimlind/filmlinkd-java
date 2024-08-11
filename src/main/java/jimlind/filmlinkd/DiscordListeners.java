package jimlind.filmlinkd;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListeners extends ListenerAdapter {
    @Override
    public void onReady(ReadyEvent e) {
        JDA jda = e.getJDA();

        String channelId = "799785154032959528";

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                UUID uuid = UUID.randomUUID();
                String message = "hello okay: " + uuid;
                jda.getTextChannelById(channelId).sendMessage(message).queue();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 5000);
    }

}
