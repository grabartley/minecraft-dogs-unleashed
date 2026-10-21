package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SleepClipRulesTest {

  private static final UUID DOG = UUID.fromString("4f3c2a10-7b6e-4d21-9a8b-1c2d3e4f5a6b");
  private static final List<String> SHIPPED_RIG =
      List.of("head_tilt", "idle", "shake", "sit", "sleep", "tail_wag", "walk");

  static Stream<Arguments> rigs() {
    return Stream.of(
        Arguments.of(SHIPPED_RIG, List.of("sleep")),
        Arguments.of(List.of("sleep_2", "idle", "sleep"), List.of("sleep", "sleep_2")),
        Arguments.of(
            List.of("sleep_10", "sleep_3", "sleep", "sleep_2"),
            List.of("sleep", "sleep_2", "sleep_3", "sleep_10")),
        Arguments.of(
            List.of("sleep", "sleep_0", "sleep_", "sleep_02", "sleepy", "sleep_side", "sit_sleep"),
            List.of("sleep")),
        Arguments.of(List.of("idle", "walk"), List.of()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("rigs")
  @DisplayName("only sleep and sleep_<n> clips are sleep variants, in number order")
  void variants(final List<String> clips, final List<String> expected) {
    assertEquals(expected, SleepClipRules.variantsOf(clips));
  }

  @ParameterizedTest(name = "seed {0}")
  @CsvSource({"0", "1", "-7", "23000", "9223372036854775807"})
  @DisplayName("a rig with only the original sleep clip always plays it")
  void shippedRigsKeepSleep(final long seed) {
    assertEquals("sleep", SleepClipRules.pick(List.of("sleep"), DOG, seed));
  }

  @Test
  @DisplayName("a rig with no sleep clip at all falls back to the sleep key")
  void noVariants() {
    assertEquals("sleep", SleepClipRules.clipFor(List.of("idle"), DOG, 15000, false, false));
  }

  @ParameterizedTest(name = "{0} variants")
  @CsvSource({"2", "3", "5"})
  @DisplayName("the pick is stable for one dog and session, and every variant gets used")
  void spreadsAcrossVariants(final int count) {
    final List<String> variants =
        Stream.concat(
                Stream.of("sleep"),
                Stream.iterate(2, n -> n + 1).limit(count - 1).map(n -> "sleep_" + n))
            .toList();
    final Set<String> seen = new HashSet<>();
    LongStream.range(0, 200)
        .forEach(
            seed -> {
              final String first = SleepClipRules.pick(variants, DOG, seed);
              assertEquals(first, SleepClipRules.pick(variants, DOG, seed));
              assertTrue(variants.contains(first));
              seen.add(first);
            });
    assertEquals(Set.copyOf(variants), seen);
  }

  @ParameterizedTest(name = "baby {0}, undead {1}: {2} and {3} share a session")
  @CsvSource({
    "false,false,13000,22999",
    "false,false,37000,46500",
    "true,false,11000,23999",
    "false,true,23000,36999",
    "true,true,24000,34999"
  })
  @DisplayName("the seed stays the same through one sleep window, even across midnight")
  void seedHoldsThroughWindow(
      final boolean baby, final boolean undead, final long start, final long end) {
    assertEquals(
        SleepClipRules.sessionSeed(start, baby, undead),
        SleepClipRules.sessionSeed(end, baby, undead));
  }

  @ParameterizedTest(name = "baby {0}, undead {1}: {2} then {3}")
  @CsvSource({
    "false,false,13000,37000",
    "false,true,0,24000",
    "false,false,1000,25000",
    "true,false,11000,35000"
  })
  @DisplayName("each new sleep window or day gets a new seed")
  void seedChangesEachWindow(
      final boolean baby, final boolean undead, final long first, final long next) {
    assertTrue(
        SleepClipRules.sessionSeed(first, baby, undead)
            != SleepClipRules.sessionSeed(next, baby, undead));
  }

  @ParameterizedTest(name = "nap at {0} and {1}")
  @CsvSource({"1000,12999", "25000,36000"})
  @DisplayName("a nap outside the sleep window keeps one seed for the whole day")
  void napSeedPerDay(final long first, final long later) {
    assertEquals(
        SleepClipRules.sessionSeed(first, false, false),
        SleepClipRules.sessionSeed(later, false, false));
  }
}
