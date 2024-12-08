package jimlind.filmlinkd.system.letterboxd;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
      // Do nothing
    }

    try {
      ZonedDateTime date = ZonedDateTime.parse(this.dateString);
      return date.toInstant().toEpochMilli();
    } catch (Exception e) {
      // Do nothing
    }

    return 0L;
  }

  public String getFormatted() {
    long timestamp = this.getMilli();
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);

    String pattern =
        Instant.now().toEpochMilli() - timestamp < 5000000000L ? "MMM dd" : "MMM dd uuu";

    return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
  }
}
