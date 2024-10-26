package jimlind.filmlinkd.system.discord.embedComponent;

import jimlind.filmlinkd.system.letterboxd.model.LBImage;
import jimlind.filmlinkd.system.letterboxd.model.LBImageSize;

public class EmbedImage {
  private LBImage image;

  public EmbedImage(LBImage image) {
    this.image = image;
  }

  public String build() {
    if (this.image == null) {
      return "";
    }

    LBImageSize emptyImage = new LBImageSize();
    emptyImage.url = "";
    emptyImage.height = 0;
    emptyImage.width = 0;

    LBImageSize tallestImage =
        this.image.sizes.stream()
            .reduce(emptyImage, (result, next) -> next.height > result.height ? next : result);

    return tallestImage.url;
  }
}
