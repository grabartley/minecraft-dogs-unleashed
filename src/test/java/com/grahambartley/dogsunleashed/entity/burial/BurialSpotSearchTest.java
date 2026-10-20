package com.grahambartley.dogsunleashed.entity.burial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BurialSpotSearchTest {

  private static final BlockPos ORIGIN = new BlockPos(100, 64, -40);

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {1L, 42L, 9001L})
  @DisplayName("with no spot anywhere it gives up after every try and every height")
  void findsNothingWhenNoSpotExists(final long seed) {
    final AtomicInteger probes = new AtomicInteger();
    final Optional<BlockPos> spot =
        BurialSpotSearch.pick(
            ORIGIN,
            Random.create(seed),
            ground -> {
              probes.incrementAndGet();
              return false;
            });
    assertTrue(spot.isEmpty());
    assertEquals(BurialSpotSearch.TRIES * (2 * BurialSpotSearch.VERTICAL_REACH + 1), probes.get());
  }

  @ParameterizedTest(name = "seed {0}")
  @ValueSource(longs = {1L, 42L, 9001L})
  @DisplayName("it scans each column from the top down and stays within the bury radius")
  void takesTheHighestSpotInRange(final long seed) {
    final BlockPos spot =
        BurialSpotSearch.pick(ORIGIN, Random.create(seed), ground -> true).orElseThrow();
    assertEquals(ORIGIN.getY() + 4, spot.getY());
    assertTrue(Math.abs(spot.getX() - ORIGIN.getX()) <= BoneCacheRules.BURY_RADIUS);
    assertTrue(Math.abs(spot.getZ() - ORIGIN.getZ()) <= BoneCacheRules.BURY_RADIUS);
  }

  @ParameterizedTest(name = "only y offset {0} is ground")
  @ValueSource(ints = {-4, -1, 0, 3})
  @DisplayName("it finds a spot at any height within reach of the origin")
  void reachesEveryHeight(final int offset) {
    final BlockPos spot =
        BurialSpotSearch.pick(
                ORIGIN, Random.create(7L), ground -> ground.getY() == ORIGIN.getY() + offset)
            .orElseThrow();
    assertEquals(ORIGIN.getY() + offset, spot.getY());
  }

  @ParameterizedTest(name = "only y offset {0} is ground")
  @ValueSource(ints = {-5, 5})
  @DisplayName("it never reaches past four blocks above or below")
  void staysWithinVerticalReach(final int offset) {
    assertTrue(
        BurialSpotSearch.pick(
                ORIGIN, Random.create(7L), ground -> ground.getY() == ORIGIN.getY() + offset)
            .isEmpty());
  }
}
