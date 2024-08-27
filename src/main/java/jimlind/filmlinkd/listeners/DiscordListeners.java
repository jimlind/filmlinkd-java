package jimlind.filmlinkd.listeners;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;

import jimlind.filmlinkd.MessageUtility;
import jimlind.filmlinkd.Queue;
import jimlind.filmlinkd.models.Message;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;

@Component
@Slf4j
public class DiscordListeners extends ListenerAdapter {
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

        ShardManager manager = jda.getShardManager();
        // There is surely a cleaner way to do this in a filter or something but
        // whatever. This works well enough.
        Boolean shardsLoggingIn = false;
        for (var entry : manager.getStatuses().entrySet()) {
            if (entry.getValue() == JDA.Status.LOGGING_IN) {
                shardsLoggingIn = true;
                break;
            }
        }
        // When shards are logging in (true) the the write only lock stays on (true)
        queue.writeOnlyLock = shardsLoggingIn;

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            // Fix this later plase....
            // Probably need to do the thing where I pass the scope to this method properly.
            // I don't like not being able to use "this.queue" or "this.messageUtility"
            public void run() {
                // Try to grab something off the queue, it is expected that it will throw an
                // exception if there isn't anything available or if all the shards aren't
                // loaded.
                String data;
                try {
                    PubsubMessage result = queue.get(jda.getShardInfo().getShardId(), manager.getShardsTotal());
                    data = result.getData().toStringUtf8();
                } catch (Exception e) {
                    return;
                }

                Message message = new GsonBuilder().create().fromJson(data, Message.class);
                ArrayList<String> channelList = messageUtility.getChannelList(message);

                for (String channelId : channelList) {
                    try {
                        TextChannel channel = jda.getTextChannelById(channelId);
                        if (channel != null) {
                            channel.sendMessage(message.entry.link).queue();
                        }
                    } catch (Exception e) {
                        String name = message.entry.userName;
                        String film = message.entry.filmTitle;
                        log.info("Unable to write message for [{}] [{}] to [{}]", name, film, channelId);

                    }
                }
            }
        };
        // I can probably make this a lot shorter for actual use
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

}
