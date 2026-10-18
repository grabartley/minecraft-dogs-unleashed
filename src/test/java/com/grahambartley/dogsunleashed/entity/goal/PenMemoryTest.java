package com.grahambartley.dogsunleashed.entity.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PenMemoryTest {

  private static final long POS = 42L;
  private static final long START = 1000L;

  private final PenMemory memory = new PenMemory();
  private final AtomicInteger scans = new AtomicInteger();

  private BooleanSupplier scanReturning(final boolean penned) {
    return () -> {
      this.scans.incrementAndGet();
      return penned;
    };
  }

  static Stream<Arguments> laterChecks() {
    return Stream.of(
        Arguments.of("the same tick", START, 1),
        Arguments.of("the last remembered tick", START + PenMemory.MEMORY_TICKS - 1, 1),
        Arguments.of("the first forgotten tick", START + PenMemory.MEMORY_TICKS, 2),
        Arguments.of("long after", START + 10_000, 2),
        Arguments.of("a clock that went backwards", START - 1, 2));
  }

  @ParameterizedTest(name = "a second check at {0} scans {2} time(s) in total")
  @MethodSource("laterChecks")
  @DisplayName("an answer is reused only while it is fresh")
  void reusesOnlyFreshAnswers(final String label, final long later, final int expectedScans) {
    this.memory.isPenned(POS, START, this.scanReturning(true));
    this.memory.isPenned(POS, later, this.scanReturning(true));
    assertEquals(expectedScans, this.scans.get());
  }

  @Test
  @DisplayName("a remembered answer is returned as it was scanned")
  void returnsTheRememberedAnswer() {
    assertTrue(this.memory.isPenned(POS, START, this.scanReturning(true)));
    assertTrue(this.memory.isPenned(POS, START + 1, this.scanReturning(false)));
    assertFalse(this.memory.isPenned(POS + 1, START, this.scanReturning(false)));
  }

  @Test
  @DisplayName("a fence placed after the memory runs out is seen")
  void seesAChangeOnceTheMemoryRunsOut() {
    assertFalse(this.memory.isPenned(POS, START, this.scanReturning(false)));
    assertTrue(this.memory.isPenned(POS, START + PenMemory.MEMORY_TICKS, this.scanReturning(true)));
  }

  @Test
  @DisplayName("stale answers are dropped once the memory fills up")
  void dropsStaleAnswersWhenFull() {
    for (long pos = 0; pos < PenMemory.PURGE_SIZE; pos++) {
      this.memory.isPenned(pos, START, this.scanReturning(false));
    }
    this.memory.isPenned(-1L, START + PenMemory.MEMORY_TICKS, this.scanReturning(false));
    assertEquals(1, this.memory.size());
  }

  @Test
  @DisplayName("fresh answers are kept when the memory fills up")
  void keepsFreshAnswersWhenFull() {
    for (long pos = 0; pos < PenMemory.PURGE_SIZE; pos++) {
      this.memory.isPenned(pos, START, this.scanReturning(false));
    }
    this.memory.isPenned(-1L, START + 1, this.scanReturning(false));
    assertEquals(PenMemory.PURGE_SIZE + 1, this.memory.size());
  }
}
