package jimlind.filmlinkd.system.google;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.logback.LoggingEventEnhancer;
import com.google.gson.Gson;
import java.util.HashMap;
import org.slf4j.event.KeyValuePair;

public class LoggingEnhancer implements LoggingEventEnhancer {

  @Override
  public void enhanceLogEntry(LogEntry.Builder logEntry, ILoggingEvent e) {
    HashMap<String, Object> map = new HashMap<>();
    map.put("thread", e.getThreadName());
    map.put("context", e.getLoggerContextVO().getName());
    map.put("logger", e.getLoggerName());

    HashMap<String, Object> metadata = new HashMap<>();
    for (KeyValuePair pair : e.getKeyValuePairs()) {
      metadata.put(pair.key, new Gson().toJson(pair.value));
    }
    map.put("metadata", metadata);

    Payload.JsonPayload payload = logEntry.build().getPayload();
    map.putAll(payload.getDataAsMap());

    logEntry.setPayload(Payload.JsonPayload.of(map));
  }
}
