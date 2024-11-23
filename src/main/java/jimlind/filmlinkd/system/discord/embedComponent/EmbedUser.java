package jimlind.filmlinkd.system.discord.embedComponent;

public class EmbedUser {
  private String userName = "";

  public EmbedUser(String userName) {
    this.userName = userName;
  }

  public String build() {
    int position = 0;
    while (true) {
      char firstChar = this.userName.charAt(position);
      char lastChar = this.userName.charAt(this.userName.length() - position - 1);

      if ((firstChar == '_') && firstChar == lastChar) {
        this.userName =
            this.userName.substring(position + 1, this.userName.length() - position - 1);
        position++;
      } else {
        if (position > 0) {
          String underscores = "\\_".repeat(position);
          this.userName = underscores + this.userName + underscores;
        }
        break;
      }
    }

    return this.userName.toLowerCase();
  }
}
