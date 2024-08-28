package jimlind.filmlinkd;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jimlind.filmlinkd.models.Message;
import jimlind.filmlinkd.models.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.furstenheim.CopyDown;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Component
@Slf4j
public class MessageUtility {
    @Autowired
    private FirestoreUtility firestoreUtility;

    public ArrayList<String> getChannelList(Message message) {
        ArrayList<String> channelList = new ArrayList<String>();
        if (!message.channelId.isBlank()) {
            channelList.add(message.channelId);
            return channelList;
        }

        try {
            return this.firestoreUtility.getUserChannelList(message.entry.userLid);
        } catch (Exception e) {
            log.info("Unable to fetch channel list from user", e);
        }

        return channelList;
    }

    public ArrayList<MessageEmbed> createEmbeds(Message message, User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();

        String profileName = user.displayName;
        String action = !message.entry.type.isEmpty() ? message.entry.type : "logg";
        String authorTitle = "%s %sed...".formatted(profileName, action);
        String profileURL = "https://letterboxd.com/%s/".formatted(user.userName);
        embedBuilder.setAuthor(authorTitle, profileURL, user.image);

        String adult = message.entry.adult ? ":underage: " : "";
        String year = message.entry.filmYear != null ? "(" + message.entry.filmYear + ")" : "";
        embedBuilder.setTitle(adult + message.entry.filmTitle + " " + year, message.entry.link);

        // Build the Review Title
        String reviewTitle = new SimpleDateFormat("**MMM dd**").format(message.entry.watchedDate);
        if (message.entry.starCount > 0){
            // Whole stars
            reviewTitle += "<:s:851134022251970610>".repeat((int) Math.floor(message.entry.starCount));
            // Half star if necessary
            reviewTitle += (message.entry.starCount % 1 > 0) ? "<:h:851199023854649374>" : "";
        }
        if (message.entry.rewatch) {
            reviewTitle += " <:r:851135667546488903>";
        }
        if (message.entry.liked) {
            reviewTitle += " <:l:851138401557676073>";
        }
        reviewTitle = !reviewTitle.isEmpty() ? reviewTitle + "\u200b\n" : "";

        // Build the Review Text
        String reviewText = message.entry.review;
        if (message.entry.review.length() > 400) {
            reviewText = reviewText.substring(0, 400).trim();
        }
        Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
        reviewText = new CopyDown().convert(reviewDocument.body().toString());
        if (message.entry.review.length() > 400) {
            reviewText += "...";
        }

        // Format Review Title and Review Text as Description
        reviewText = message.entry.containsSpoilers.equals("true") ? "||" + reviewText + "||" : reviewText;
        reviewText = reviewText.replaceAll("[\r\n]+", "\n");
        String rule = reviewTitle.length() > 1 && reviewText.length() > 1 ? "┈".repeat(12) + "\n" : "";
        embedBuilder.setDescription(reviewTitle + rule + reviewText);

        // If there is footer data with actual data then include it.
        // TODO: Why doesn't this work?!?!
        if (user.footer != null && !user.footer.text.isBlank()) {
            embedBuilder.setFooter(user.footer.text, user.footer.icon);
        }

        embedBuilder.setThumbnail(message.entry.image);
        embedBuilder.setColor(new Color(0xa700bd));

        ArrayList<MessageEmbed> collection = new ArrayList<MessageEmbed>();
        collection.add(embedBuilder.build());

        return collection;
    }
}
