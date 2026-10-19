package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PupGrowthClockTest {

  @ParameterizedTest(name = "{0}% over {1} ticks -> {2} ticks of growth")
  @CsvSource({
    "0, 1000, 0",
    "33, 300, 99",
    "50, 1000, 500",
    "100, 1000, 1000",
    "200, 1000, 2000",
    "500, 10, 50",
  })
  @DisplayName("over many ticks a pup grows by the growth speed share of vanilla aging")
  void growsByTheGrowthSpeedShare(final int growthRate, final int ticks, final int expected) {
    final PupGrowthClock clock = new PupGrowthClock();
    int grown = 0;
    for (int tick = 0; tick < ticks; tick++) {
      grown += clock.tick(growthRate);
    }
    assertEquals(expected, grown);
  }

  @Test
  @DisplayName("part of a tick carries over to the next tick")
  void carriesPartOfATickOver() {
    final PupGrowthClock clock = new PupGrowthClock();
    assertEquals(0, clock.tick(50));
    assertEquals(1, clock.tick(50));
    assertEquals(1, clock.tick(150));
    assertEquals(2, clock.tick(150));
  }

  @Test
  @DisplayName("a reset drops the carried part of a tick")
  void resetDropsTheCarry() {
    final PupGrowthClock clock = new PupGrowthClock();
    clock.tick(50);
    clock.reset();
    assertEquals(0, clock.tick(50));
  }
}
