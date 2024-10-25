package jimlind.filmlinkd.system.discord.embedComponent;

public class EmbedRunTime {
  private int runTime;

  public EmbedRunTime(int runTime) {
    this.runTime = runTime;
  }

  public String build() {
    double hours = Math.floor((double) this.runTime / 60);
    double minutes = this.runTime - hours * 60;

    return String.format("%.0fh %.0fm", hours, minutes);
  }
}
