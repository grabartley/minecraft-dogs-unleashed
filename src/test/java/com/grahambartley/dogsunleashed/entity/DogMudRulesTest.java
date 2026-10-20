package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.DogMudRules.MudState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogMudRulesTest {

  private static final MudState MUDDY = new MudState(true, 0, 0, 0);

  private static MudState run(
      final MudState start,
      final int ticks,
      final boolean enabled,
      final boolean dirtying,
      final boolean cleanWater) {
    MudState state = start;
    for (int i = 0; i < ticks; i++) {
      state = DogMudRules.next(state, enabled, dirtying, cleanWater);
    }
    return state;
  }

  @ParameterizedTest(name = "{0} ticks on dirty ground leaves the dog muddy: {1}")
  @CsvSource({"1, false", "20, false", "39, false", "40, true", "200, true"})
  @DisplayName("a dog gets muddy after a short time on dirty ground")
  void getsMuddyAfterExposure(final int ticks, final boolean muddy) {
    assertEquals(muddy, run(DogMudRules.CLEAN, ticks, true, true, false).muddy());
  }

  @Test
  @DisplayName("stepping off dirty ground before it sticks keeps the dog clean")
  void briefExposureResets() {
    final MudState almost = run(DogMudRules.CLEAN, 39, true, true, false);
    final MudState off = DogMudRules.next(almost, true, false, false);
    assertEquals(DogMudRules.CLEAN, off);
    assertFalse(DogMudRules.next(off, true, true, false).muddy());
  }

  @ParameterizedTest(name = "dirty={0}, clean water={1}")
  @CsvSource({"true, false", "false, false", "false, true", "true, true"})
  @DisplayName("with the setting off a dog is always clean")
  void disabledIsClean(final boolean dirtying, final boolean cleanWater) {
    assertEquals(DogMudRules.CLEAN, DogMudRules.next(MUDDY, false, dirtying, cleanWater));
  }

  @ParameterizedTest(name = "{0} ticks swimming washes the dog: {1}")
  @CsvSource({"1, false", "199, false", "200, true"})
  @DisplayName("swimming in clean water washes the mud off over time")
  void swimmingWashes(final int ticks, final boolean washed) {
    assertEquals(!washed, run(MUDDY, ticks, true, false, true).muddy());
  }

  @Test
  @DisplayName("leaving the water before it is clean starts the soak again")
  void soakResetsOutOfWater() {
    final MudState soaked = run(MUDDY, 199, true, false, true);
    final MudState dried = DogMudRules.next(soaked, true, false, false);
    assertEquals(0, dried.soakTicks());
    assertTrue(DogMudRules.next(dried, true, false, true).muddy());
  }

  @ParameterizedTest(name = "{0} ticks after getting muddy it is still muddy: {1}")
  @CsvSource({"24000, true", "71999, true", "72000, false"})
  @DisplayName("mud wears off by itself after three in-game days")
  void wearsOff(final int ticks, final boolean muddy) {
    assertEquals(muddy, run(MUDDY, ticks, true, false, false).muddy());
  }

  @Test
  @DisplayName("staying on dirty ground keeps the mud fresh")
  void dirtyGroundRefreshes() {
    final MudState old = new MudState(true, 0, DogMudRules.TICKS_TO_WEAR_OFF - 1, 0);
    final MudState fresh = run(old, DogMudRules.TICKS_TO_GET_MUDDY, true, true, false);
    assertTrue(fresh.muddy());
    assertEquals(0, fresh.muddyTicks());
  }

  @ParameterizedTest(name = "muddy={0}, {1} blocks away, roll {2} -> spatters: {3}")
  @CsvSource({
    "true, 1.0, 0, true",
    "true, 2.5, 49, true",
    "true, 2.5, 50, false",
    "true, 2.6, 0, false",
    "false, 1.0, 0, false"
  })
  @DisplayName("a muddy dog shaking next to you sometimes spatters you")
  void spatters(
      final boolean muddy, final double distance, final int roll, final boolean expected) {
    assertEquals(expected, DogMudRules.spatters(muddy, distance, roll));
  }

  @ParameterizedTest(name = "a clean dog out of the water for {0} ticks stays clean")
  @ValueSource(ints = {1, 100, 100_000})
  @DisplayName("a clean dog away from mud stays clean")
  void cleanStaysClean(final int ticks) {
    assertEquals(DogMudRules.CLEAN, run(DogMudRules.CLEAN, ticks, true, false, false));
  }
}
