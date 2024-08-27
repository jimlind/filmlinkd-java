package jimlind.filmlinkd;

import java.awt.Color;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.furstenheim.CopyDown;
import jimlind.filmlinkd.models.Message;
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

    public ArrayList<MessageEmbed> createEmbeds(Message message) {
        EmbedBuilder embedBuilder = new EmbedBuilder().setAuthor(message.entry.userName);

        embedBuilder.setAuthor("This user did this action", "http://www.google.com",
                "https://jimlind.github.io/filmlinkd/images/greta-100.png");

        String adult = message.entry.adult ? ":underage: " : "";
        String year = message.entry.filmYear != null ? "(" + message.entry.filmYear + ")" : "";
        embedBuilder.setTitle(adult + message.entry.filmTitle + " " + year);

        String reviewText = message.entry.review;
        if (message.entry.review.length() > 400) {
            reviewText = reviewText.substring(0, 400).trim();
        }
        Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
        reviewText = new CopyDown().convert(reviewDocument.body().toString());
        if (message.entry.review.length() > 400) {
            reviewText += "...";
        }
        String reviewTitle = "Aug 14 <:r:851135667546488903> <:l:851138401557676073>";
        reviewTitle = reviewTitle.length() > 0 ? reviewTitle + "\u200b\n" : "";

        reviewText = message.entry.containsSpoilers == "true" ? "||" + reviewText + "||" : reviewText;
        reviewText = reviewText.replaceAll("[\r\n]+", "\n");

        String rule = reviewTitle.length() > 1 && reviewText.length() > 1 ? "┈".repeat(12) + "\n" : "";

        embedBuilder.setDescription(reviewTitle + rule + reviewText);
        embedBuilder.setThumbnail(message.entry.image);
        embedBuilder.setColor(new Color(0xa700bd));

        ArrayList<MessageEmbed> collection = new ArrayList<MessageEmbed>();
        collection.add(embedBuilder.build());

        return collection;
    }
}
