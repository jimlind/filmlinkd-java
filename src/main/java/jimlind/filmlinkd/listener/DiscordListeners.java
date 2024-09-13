package jimlind.filmlinkd.listener;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.MessageUtility;
import jimlind.filmlinkd.Queue;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

@Component
@Slf4j
public class DiscordListeners extends ListenerAdapter {
    @Autowired
    private FirestoreManager firestoreManager;
    @Autowired
    private MessageUtility messageUtility;
    @Autowired
    private Queue queue;
    @Autowired
    private UserFactory userFactory;

    @Override
    public void onReady(ReadyEvent e) {
        JDA jda = e.getJDA();

        ShardManager manager = jda.getShardManager();
        // There is surely a cleaner way to do this in a filter or something but
        // whatever. This works well enough.
        boolean shardsLoggingIn = false;
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
            // Fix this later please....
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

                User user = null;
                Message message = new GsonBuilder().create().fromJson(data, Message.class);
                try {
                    QueryDocumentSnapshot userDocument = firestoreManager.getUserDocument(message.entry.userLid);
                    user = userFactory.createFromDocument(userDocument);
                } catch (Exception e) {
                    log.warn("Invalid user [{}] passed in PubSub message", message.entry.userLid);
                }
                ArrayList<String> channelList = messageUtility.getChannelList(message);

                for (String channelId : channelList) {
                    try {
                        GuildMessageChannel channel = jda.getChannelById(GuildMessageChannel.class, channelId);
                        if (channel != null && user != null) {
                            channel.sendMessageEmbeds(messageUtility.createEmbeds(message, user)).queue();
                        }
                    } catch (Exception e) {
                        String name = message.entry.userName;
                        String film = message.entry.filmTitle;
                        log.info("Unable to write message for [{}] [{}] to [{}]", name, film, channelId, e);
                        log.info("Message write failure", e);
                        log.error("e: ", e);
                    }
                }
            }
        };
        // I can probably make this a lot shorter for actual use
        timer.scheduleAtFixedRate(task, 0, 1000);
    }

}
