package jimlind.filmlinkd.system.discord;

import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountHelper {
  @Autowired private MemberAPI memberAPI;
  @Autowired private MemberWeb memberWeb;

  @Nullable
  public LBMember getMember(SlashCommandInteractionEvent event) {
    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(userName);

    return this.memberAPI.fetch(userLID);
  }
}
