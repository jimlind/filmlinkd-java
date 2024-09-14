package jimlind.filmlinkd.listener;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.messageEmbed.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.google.PubSubQueue;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.internal.entities.ReceivedMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

// This Class is now too big but I don't know what to name the things that are in it otherwise yet.
@Component
@Slf4j
public class DiscordListeners extends ListenerAdapter {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private DiaryEntryEmbedFactory diaryEntryEmbedFactory;
  @Autowired private PubSubQueue pubSubQueue;
  @Autowired private UserFactory userFactory;

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
    // When shards are logging in (true) the write only lock stays on (true)
    pubSubQueue.writeOnlyLock = shardsLoggingIn;

    Timer timer = new Timer();
    TimerTask task =
        new TimerTask() {
          // Fix this later please....
          // Probably need to do the thing where I pass the scope to this method properly.
          // I don't like not being able to use "this.queue" or "this.messageUtility"
          public void run() {
            // Try to grab something off the queue, it is expected that it will throw an
            // exception if there isn't anything available or if all the shards aren't
            // loaded.
            String data;
            try {
              PubsubMessage result =
                  pubSubQueue.get(jda.getShardInfo().getShardId(), manager.getShardsTotal());
              data = result.getData().toStringUtf8();
            } catch (Exception e) {
              return;
            }

            User user = null;
            Message message = new GsonBuilder().create().fromJson(data, Message.class);
            try {
              QueryDocumentSnapshot userDocument =
                  firestoreManager.getUserDocument(message.entry.userLid);
              user = userFactory.createFromSnapshot(userDocument);
            } catch (Exception e) {
              log.warn("Invalid user [{}] passed in PubSub message", message.entry.userLid);
            }
            ArrayList<String> channelList = getChannelListFromMessage(message);

            for (String channelId : channelList) {
              try {
                GuildMessageChannel channel =
                    jda.getChannelById(GuildMessageChannel.class, channelId);
                if (channel != null && user != null) {
                  channel
                      .sendMessageEmbeds(diaryEntryEmbedFactory.create(message, user))
                      .queue(
                          (recievedMessage) ->
                              sendSuccess((ReceivedMessage) recievedMessage, message.entry));
                }
              } catch (Exception e) {
                String name = message.entry.userName;
                String film = message.entry.filmTitle;
                log.info("Unable to write message for [{}] [{}] to [{}]", name, film, channelId, e);
              }
            }
          }
        };
    // I can probably make this a lot shorter for actual use
    timer.scheduleAtFixedRate(task, 0, 1000);
  }

  private void sendSuccess(ReceivedMessage receivedMessage, Message.Entry entry) {
    boolean updateSuccess =
        firestoreManager.updateUserPrevious(
            entry.userLid, entry.lid, entry.publishedDate, entry.link);
    if (updateSuccess) {
      log.info("Successfully sent message");
    }
  }

  private ArrayList<String> getChannelListFromMessage(Message message) {
    ArrayList<String> channelList = new ArrayList<String>();
    if (!message.channelId.isBlank()) {
      channelList.add(message.channelId);
      return channelList;
    }

    try {
      QueryDocumentSnapshot document = this.firestoreManager.getUserDocument(message.entry.userLid);
      User user = this.userFactory.createFromSnapshot(document);
      return user.getChannelList();
    } catch (Exception e) {
      log.info("Unable to fetch channel list from user", e);
    }

    return channelList;
  }
}
