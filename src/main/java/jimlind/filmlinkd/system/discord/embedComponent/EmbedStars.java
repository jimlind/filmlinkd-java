package jimlind.filmlinkd.system.discord.embedComponent;

public class EmbedStars {
  private final float starCount;

  public EmbedStars(float starCount) {
    this.starCount = starCount;
  }

  public String build() {
    if (this.starCount == 0) {
      return "";
    }
    String starString = "<:s:851134022251970610>".repeat((int) Math.floor(this.starCount));
    starString += (this.starCount % 1 > 0) ? "<:h:851199023854649374>" : "";

    return starString;
  }
}
