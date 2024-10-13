package jimlind.filmlinkd.system.discord.embedComponent;

public class EmbedDescription {
  private String descriptionText = "";

  public EmbedDescription(String descriptionText) {
    this.descriptionText = descriptionText;
  }

  public String build() {
    int endIndex = Math.min(this.descriptionText.length(), 4096);
    return this.descriptionText.substring(0, endIndex);
  }
}
