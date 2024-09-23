package jimlind.filmlinkd.system.google;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.logback.LoggingEventEnhancer;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import org.slf4j.event.KeyValuePair;

public class LoggingEnhancer implements LoggingEventEnhancer {

  @Override
  public void enhanceLogEntry(LogEntry.Builder logEntryBuilder, ILoggingEvent loggingEvent) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("thread", loggingEvent.getThreadName());
    map.put("context", loggingEvent.getLoggerContextVO().getName());
    map.put("logger", loggingEvent.getLoggerName());

    List<KeyValuePair> valueList = loggingEvent.getKeyValuePairs();
    HashMap<String, Object> metadata = new HashMap<>();
    if (valueList != null) {
      for (KeyValuePair pair : valueList) {
        metadata.put(pair.key, parseValue(pair.value));
      }
      map.put("metadata", metadata);
    }

    Payload.JsonPayload payload = logEntryBuilder.build().getPayload();
    map.putAll(payload.getDataAsMap());

    logEntryBuilder.setPayload(Payload.JsonPayload.of(map));
  }

  private Object parseValue(Object input) {
    if (input instanceof String
        || input instanceof Integer
        || input instanceof Long
        || input instanceof Float
        || input instanceof Double) {
      return input;
    }

    try {
      return new Gson().toJson(input);
    } catch (Exception e) {
      // Do Nothing. This can fail for a multitude of reasonable reasons
    }

    try {
      return input.toString();
    } catch (Exception e) {
      return e.toString();
    }
  }
}
