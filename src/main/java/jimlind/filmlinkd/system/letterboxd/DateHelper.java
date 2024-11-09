package jimlind.filmlinkd.system.letterboxd;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class DateHelper {
  private final String dateString;

  public DateHelper(String dateString) {
    this.dateString = dateString;
  }

  public long getMilli() {
    try {
      LocalDate date = LocalDate.parse(this.dateString);
      return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    } catch (Exception e) {
      return 0L;
    }
  }
}
