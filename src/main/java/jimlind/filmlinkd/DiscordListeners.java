package jimlind.filmlinkd;

import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;

import jimlind.filmlinkd.models.Message;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListeners extends ListenerAdapter {

    private Queue queue;

    public DiscordListeners(Queue localQueue) {
        queue = localQueue;
    }

    @Override
    public void onReady(ReadyEvent e) {
        JDA jda = e.getJDA();

        String channelId = "799785154032959528";

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            public void run() {
                // Try to grab something off the queue
                String data;
                try {
                    PubsubMessage result = queue.get();
                    data = result.getData().toStringUtf8();
                } catch (Exception e) {
                    return;
                }

                Message message = new GsonBuilder().create().fromJson(data, Message.class);
                jda.getTextChannelById(channelId).sendMessage(message.entry.link).queue();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

}
