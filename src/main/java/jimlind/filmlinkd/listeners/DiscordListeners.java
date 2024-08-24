package jimlind.filmlinkd.listeners;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;

import jimlind.filmlinkd.MessageUtility;
import jimlind.filmlinkd.Queue;
import jimlind.filmlinkd.models.Message;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class DiscordListeners extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DiscordListeners.class);
    private MessageUtility messageUtility;
    private Queue queue;

    @Autowired
    public DiscordListeners(MessageUtility messageUtility, Queue queue) {
        this.messageUtility = messageUtility;
        this.queue = queue;
    }

    @Override
    public void onReady(ReadyEvent e) {
        JDA jda = e.getJDA();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            // Fix this later plase....
            // Probably need to do the thing where I pass the scope to this method properly.
            // I don't like not being able to use "this.queue" or "this.messageUtility"
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
                ArrayList<String> channelList = messageUtility.getChannelList(message);

                for (String channelId : channelList) {
                    try {
                        jda.getTextChannelById(channelId).sendMessage(message.entry.link).queue();
                    } catch (Exception e) {
                        logger.info(
                                "Unable to write message for [" + message.entry.filmTitle + "] to [" + channelId + "]");
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

}
