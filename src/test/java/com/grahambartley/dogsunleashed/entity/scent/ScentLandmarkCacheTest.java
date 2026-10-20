package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ScentLandmarkCacheTest {

  private static final ScentLandmark RUINS = new ScentLandmark(512, -96);

  private final AtomicInteger lookups = new AtomicInteger();

  private Supplier<Optional<ScentLandmark>> counting(final Optional<ScentLandmark> answer) {
    return () -> {
      this.lookups.incrementAndGet();
      return answer;
    };
  }

  @ParameterizedTest(name = "{0} resolves")
  @ValueSource(ints = {1, 2, 20, 400})
  @DisplayName("a found landmark is looked up once however often the dog asks")
  void looksUpAFoundLandmarkOnce(final int resolves) {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    for (int i = 0; i < resolves; i++) {
      assertEquals(Optional.of(RUINS), cache.resolve(this.counting(Optional.of(RUINS))));
    }
    assertEquals(1, this.lookups.get());
  }

  @Test
  @DisplayName("a failed lookup is remembered too, so it is not retried")
  void remembersAFailedLookup() {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    cache.resolve(this.counting(Optional.empty()));
    cache.resolve(this.counting(Optional.of(RUINS)));
    assertEquals(1, this.lookups.get());
    assertTrue(cache.known().isEmpty());
  }

  @Test
  @DisplayName("a remembered map mark never triggers a lookup")
  void rememberedMarkSkipsLookup() {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    cache.remember(RUINS);
    assertEquals(Optional.of(RUINS), cache.resolve(this.counting(Optional.empty())));
    assertEquals(0, this.lookups.get());
  }

  @Test
  @DisplayName("a new scent clears the cache so the next one looks up again")
  void clearingAllowsOneMoreLookup() {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    cache.resolve(this.counting(Optional.of(RUINS)));
    cache.clear();
    assertTrue(cache.known().isEmpty());
    cache.resolve(this.counting(Optional.of(RUINS)));
    cache.resolve(this.counting(Optional.of(RUINS)));
    assertEquals(2, this.lookups.get());
  }
}
