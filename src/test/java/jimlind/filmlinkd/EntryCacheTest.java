package jimlind.filmlinkd;

import org.junit.Test;

import static org.junit.Assert.assertSame;

public class EntryCacheTest {
  @Test
  public void basicCacheTest() {
    EntryCache cache = new EntryCache();
    cache.set("foo");

    Object barResult = cache.get("bar");
    Object fooResult = cache.get("foo");

    assertSame(false, barResult);
    assertSame(true, fooResult);
  }
}
