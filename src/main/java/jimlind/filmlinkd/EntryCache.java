package jimlind.filmlinkd;

import com.github.benmanes.caffeine.cache.*;
import org.springframework.stereotype.Component;

@Component
public class EntryCache {
  Cache<String, Boolean> cache;

  public EntryCache() {
    this.cache = Caffeine.newBuilder().maximumSize(10_000).build();
  }

  public void set(String key) {
    this.cache.put(key, true);
  }

  public Boolean get(String key) {
    Boolean result = this.cache.getIfPresent(key);
    return result != null;
  }
}
