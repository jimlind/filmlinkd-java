package jimlind.filmlinkd.listener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.filmlinkd.EntryCache;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.messageEmbed.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.google.FirestoreManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
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
public class DiscordListener extends ListenerAdapter {
  @Autowired private DiaryEntryEmbedFactory diaryEntryEmbedFactory;
  @Autowired private EntryCache entryCache;
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private ScrapedResultQueue scrapedResultQueue;
  @Autowired private UserFactory userFactory;

  @Override
  public void onReady(ReadyEvent e) {
    JDA jda = e.getJDA();

    ShardManager manager = jda.getShardManager();
    if (manager == null) {
      log.error("Problem Getting ShardManager");
      return;
    }

    log.info("Discord Client Logged In on {} Servers", jda.getGuildCache().size());
    int shardId = jda.getShardInfo().getShardId();

    Timer timer = new Timer();
    TimerTask task =
        new TimerTask() {
          // Fix this later please....
          // Probably need to do the thing where I pass the scope to this method properly.
          // I don't like not being able to use "this.queue" or "this.messageUtility"
          public void run() {
            ScrapedResult result = scrapedResultQueue.get(shardId, manager.getShardsTotal());
            if (result == null) {
              return;
            }

            Message message = result.message;
            User user = result.user;

            // TODO: Move this cache to message receiver and remove the shard id
            // We are expecting multiple requests to post a diary entry so we maintain the one
            // source of truth on the server that sends messages. We keep an in memory cache.
            String key = message.entry.lid + '-' + shardId;
            // If there isn't a specific channel to send to and key is in the cache skip it
            // TODO: Use the new has override on the message
            if (message.channelId.isBlank() && entryCache.get(key)) {
              return;
            } else {
              // Assume that write will succeed so write to cache. If it actually doesn't succeed
              // then it'll get tried again some later time, but this is designed to limit
              // duplicates from scrape events
              entryCache.set(key);
            }

            ArrayList<MessageEmbed> embedList;
            try {
              embedList = diaryEntryEmbedFactory.create(message, user);
            } catch (Exception e) {
              log.atError()
                  .setMessage("Creating Diary Entry Embed Failed")
                  .addKeyValue("message", message)
                  .addKeyValue("user", user)
                  .log();
              return;
            }

            for (String channelId : getChannelListFromScrapeResult(result)) {
              GuildMessageChannel channel =
                  jda.getChannelById(GuildMessageChannel.class, channelId);

              // Not finding a channel is extremely normal when running shards so we skip it
              if (channel == null) {
                continue;
              }

              // Not having proper permissions is more normal than it should be so we skip it
              Member self = channel.getGuild().getSelfMember();
              if (!self.hasPermission(
                  channel,
                  Permission.VIEW_CHANNEL,
                  Permission.MESSAGE_SEND,
                  Permission.MESSAGE_EMBED_LINKS)) {
                continue;
              }

              try {
                channel
                    .sendMessageEmbeds(embedList)
                    .queue(
                        m -> sendSuccess(m, message, channel), m -> sendFailure(message, channel));
              } catch (Exception e) {
                log.atError()
                    .setMessage("Send MessageEmbeds Failed")
                    .addKeyValue("shard", shardId)
                    .addKeyValue("message", message)
                    .addKeyValue("channel", channelId)
                    .addKeyValue("exception", e.toString())
                    .log();
              }
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
        .addKeyValue("delay", Instant.now().toEpochMilli() - entry.publishedDate)
        .addKeyValue("source", entry.publishSource)
        .log();

    // Log a too much information about the successfully sent message
    log.atInfo()
        .setMessage("Successfully Sent Message")
        .addKeyValue("channelId", channel.getId())
        .addKeyValue("message", message)
        .addKeyValue("channel", channel)
        .addKeyValue("jdaMessage", jdaMessage)
        .log();

    boolean updateSuccess =
        firestoreManager.updateUserPrevious(
            entry.userLid, entry.lid, entry.publishedDate, entry.link);

    if (!updateSuccess) {
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

  private ArrayList<String> getChannelListFromScrapeResult(ScrapedResult scrapedResult) {
    // TODO: If the entry in the scraped result is newer than user's previous then send to the
    // complete channel list
    ArrayList<String> channelList = new ArrayList<String>();
    String channelId = scrapedResult.message.channelId;
    // TODO: Use new hasOverride on the message
    if (!channelId.isBlank()) {
      channelList.add(channelId);
      return channelList;
    }

    try {
      return scrapedResult.user.getChannelList();
    } catch (Exception e) {
      log.info("Unable to fetch channel list from user", e);
    }

    return channelList;
  }
}
