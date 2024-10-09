package jimlind.filmlinkd.listener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import jimlind.filmlinkd.factory.messageEmbed.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
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
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private ScrapedResultQueue scrapedResultQueue;

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

              // Not finding a channel is extremely normal when running shards so we ignore the
              // possible issue and don't log anything
              if (channel == null) {
                continue;
              }

              // Not having proper permissions is more normal than it should be so we ignore the
              // possible issue and don't log anything
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
                        m -> sendSuccess(m, result, channel), m -> sendFailure(message, channel));
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
      ScrapedResult scrapedResult,
      GuildMessageChannel channel) {
    Message.Entry entry = scrapedResult.message.entry;

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
        .addKeyValue("message", scrapedResult.message)
        .addKeyValue("channel", channel)
        .addKeyValue("jdaMessage", jdaMessage)
        .log();

    // If the entry doesn't exist in the users previous entry list write it
    if (!scrapedResult.user.previous.list.contains(entry.lid)) {
      boolean updateSuccess =
          firestoreManager.updateUserPrevious(
              entry.userLid, entry.lid, entry.publishedDate, entry.link);

      if (!updateSuccess) {
        log.atError()
            .setMessage("Entry did not Update")
            .addKeyValue("entry", scrapedResult.message.entry)
            .log();
      }
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
    ArrayList<String> channelList = new ArrayList<String>();
    Message message = scrapedResult.message;
    String previous = scrapedResult.user.getMostRecentPrevious();
    boolean isNewerThanKnown = LidComparer.compare(previous, scrapedResult.message.entry.lid) < 0;

    if (message.hasChannelOverride() && !isNewerThanKnown) {
      channelList.add(message.channelId);
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
