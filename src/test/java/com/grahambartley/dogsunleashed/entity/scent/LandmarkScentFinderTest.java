package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import net.minecraft.server.world.ServerWorld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LandmarkScentFinderTest {

  private static final ScentLandmark RUINS = new ScentLandmark(-700, 1312);

  private final AtomicInteger lookups = new AtomicInteger();

  private Function<ServerWorld, Optional<ScentLandmark>> counting(
      final Optional<ScentLandmark> answer) {
    return world -> {
      this.lookups.incrementAndGet();
      return answer;
    };
  }

  @ParameterizedTest(name = "{0} restarted searches")
  @ValueSource(ints = {1, 3, 50})
  @DisplayName("restarting the search for one scent never looks the structure up again")
  void restartsShareOneLookup(final int restarts) {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    for (int i = 0; i < restarts; i++) {
      final LandmarkScentFinder finder =
          new LandmarkScentFinder(null, cache, this.counting(Optional.of(RUINS)));
      for (int tick = 0; tick < 5; tick++) {
        assertEquals(ScentShellScan.Status.FOUND, finder.advance(null));
      }
    }
    assertEquals(1, this.lookups.get());
  }

  @Test
  @DisplayName("no structure in range means the search is over")
  void missingStructureIsExhausted() {
    final LandmarkScentFinder finder =
        new LandmarkScentFinder(null, new ScentLandmarkCache(), this.counting(Optional.empty()));
    assertEquals(ScentShellScan.Status.EXHAUSTED, finder.advance(null));
    assertEquals(ScentShellScan.Status.EXHAUSTED, finder.advance(null));
    assertEquals(1, this.lookups.get());
  }

  @Test
  @DisplayName("a map mark is found straight away without any lookup")
  void knownMarkNeedsNoLookup() {
    final ScentLandmarkCache cache = new ScentLandmarkCache();
    cache.remember(RUINS);
    final LandmarkScentFinder finder =
        new LandmarkScentFinder(null, cache, this.counting(Optional.empty()));
    assertEquals(ScentShellScan.Status.FOUND, finder.advance(null));
    assertEquals(ScentTargetKind.LANDMARK, finder.kind());
    assertEquals(0, this.lookups.get());
  }
}
