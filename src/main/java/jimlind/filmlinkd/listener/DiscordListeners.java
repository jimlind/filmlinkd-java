package jimlind.filmlinkd.listener;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.messageEmbed.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.google.PubSubQueue;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// This Class is now too big, but I don't know what to name the things that are in it otherwise yet.
// There's also something funky here that google-java-format says it can't parse this file.

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
    if (manager == null) {
      log.error("Problem Getting ShardManager");
      return;
    }

    log.info("Discord Client Logged In on {} Servers", manager.getGuildCache().size());
    int shardId = jda.getShardInfo().getShardId();

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
              PubsubMessage result = pubSubQueue.get(shardId, manager.getShardsTotal());
              data = result.getData().toStringUtf8();
            } catch (Exception e) {
              // Don't log or raise because expected. Yes this is bad, so it'll get fixed later
              return;
            }

            // Translate to a message object
            Message message = new GsonBuilder().create().fromJson(data, Message.class);

            // Attempt to get user based on Message
            QueryDocumentSnapshot snapshot;
            try {
              snapshot = firestoreManager.getUserDocument(message.entry.userLid);
            } catch (Exception e) {
              log.atWarn()
                  .setMessage("Invalid User Passed in PubSub Message")
                  .addKeyValue("message", message)
                  .log();
              return;
            }

            // Translate to user object
            User user = userFactory.createFromSnapshot(snapshot);
            if (user == null) {
              log.atWarn()
                  .setMessage("Unable to Create User from Snapshot")
                  .addKeyValue("message", message)
                  .addKeyValue("snapshot", snapshot)
                  .log();
              return;
            }

            for (String channelId : getChannelListFromMessage(message)) {
              GuildMessageChannel channel =
                  jda.getChannelById(GuildMessageChannel.class, channelId);

              if (channel == null) {
                log.atWarn()
                    .setMessage("Unable to Find Channel")
                    .addKeyValue("channelId", channelId)
                    .addKeyValue("message", message)
                    .log();
                continue;
              }

              ArrayList<MessageEmbed> embedList;
              try {
                embedList = diaryEntryEmbedFactory.create(message, user);
              } catch (Exception e) {
                log.atWarn()
                    .setMessage("Creating Diary Entry Embed Failed")
                    .addKeyValue("message", message)
                    .addKeyValue("user", user)
                    .log();
                continue;
              }

              channel
                  .sendMessageEmbeds(embedList)
                  .queue(m -> sendSuccess(m, message, channel), m -> sendFailure(message, channel));
            }
          }
        };
    // I can probably make this a lot shorter for actual use
    timer.scheduleAtFixedRate(task, 0, 1000);
  }

  private void sendSuccess(
      net.dv8tion.jda.api.entities.Message jdaMessage,
      Message message,
      GuildMessageChannel channel) {
    Message.Entry entry = message.entry;

    // Log delay time between now and published time
    log.atInfo()
        .setMessage("Entry Publish Delay")
        .addKeyValue("delay", Instant.now().toEpochMilli() - entry.updatedDate)
        .addKeyValue("source", entry.publishSource)
        .log();

    // Log a too much information about the successfully sent message
    log.atDebug()
        .setMessage("Successfully Sent Message")
        .addKeyValue("channel", channel)
        .addKeyValue("message", message)
        .addKeyValue("jdaMessage", jdaMessage)
        .log();

    boolean updateSuccess =
        firestoreManager.updateUserPrevious(
            entry.userLid, entry.lid, entry.publishedDate, entry.link);

    if (!updateSuccess) {
      System.out.println("boog");
      log.atError().setMessage("Entry did not Update").addKeyValue("entry", message.entry).log();
    }
  }

  private void sendFailure(Message message, GuildMessageChannel channel) {
    log.atWarn()
        .setMessage("Failed to Send Message")
        .addKeyValue("message", message)
        .addKeyValue("channel", channel)
        .log();
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
