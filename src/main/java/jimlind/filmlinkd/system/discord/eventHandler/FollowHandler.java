package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.messageEmbed.FollowEmbedFactory;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.ChannelHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.letterboxd.LidComparer;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FollowHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private FollowEmbedFactory followEmbedFactory;
  @Autowired private LogEntriesAPI logEntriesAPI;
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;
  @Autowired private PubSubManager pubSubManager;

  public String getEventName() {
    return "follow";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.toString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(userName);
    LBMember member = this.memberAPI.fetch(userLID);

    if (member == null) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("No Results Found").queue();
      return;
    }

    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      // TODO: Log empty response
      // TODO: Extract the no results to another method
      event.getHook().sendMessage("Channel Not Found").queue();
      return;
    }

    QueryDocumentSnapshot snapshot = this.firestoreManager.getUserDocument(member.id);
    User user = null;
    // Create the user in the database if it doesn't exist
    if (snapshot == null) {
      this.firestoreManager.createUserDocument(member);
    } else {
      user = new UserFactory().createFromSnapshot(snapshot);
    }

    if (!this.firestoreManager.addUserSubscription(member.id, channelId)) {
      // TODO: Log something maybe
      // TODO: Extract failure messages to another method
      event.getHook().sendMessage("Follow Failed").queue();
      return;
    }

    List<LBLogEntry> logEntryList = this.logEntriesAPI.getRecentForUser(member.id, 1);
    if (logEntryList.size() == 1) {
      LBLogEntry logEntry = logEntryList.get(0);

      Command command = new Command();
      command.command = "FOLLOW";
      command.user = logEntry.owner.id;
      command.entry = logEntry.id;
      this.pubSubManager.publishCommand(command);

      Message.PublishSource source = Message.PublishSource.Follow;
      Message message = new MessageFactory().createFromLogEntry(logEntry, source);
      // Only add the channel if we know that the log entry has already been posted
      // Including a channel id is a signal to the MessageReceiver to only send to one channel
      if (user != null && LidComparer.compare(logEntry.id, user.getMostRecentPrevious()) <= 0) {
        message.channelId = channelId;
      }
      this.pubSubManager.publishLogEntry(message);
    }

    ArrayList<MessageEmbed> messageEmbedList = this.followEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
