package jimlind.filmlinkd.system.discord.embedComponent;

import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.system.letterboxd.model.LBContributionType;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmContributions;

public class EmbedDirectors {
  public LBFilmContributions contributions;

  public EmbedDirectors(List<LBFilmContributions> contributionList) {
    List<LBFilmContributions> filteredContributions =
        contributionList.stream()
            .filter(contributions -> contributions.type.equals(LBContributionType.Director))
            .toList();
    if (!filteredContributions.isEmpty()) {
      this.contributions = filteredContributions.get(0);
    }
  }

  public String build() {
    String directorLinks =
        contributions.contributors.stream()
            .map(c -> String.format("[%s](https://boxd.it/%s)", c.name, c.id))
            .collect(Collectors.joining(", "));

    return "Director(s): " + directorLinks;
  }
}
