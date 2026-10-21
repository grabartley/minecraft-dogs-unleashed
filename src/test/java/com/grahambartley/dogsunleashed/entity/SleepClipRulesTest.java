package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SleepClipRulesTest {

  private static final UUID DOG = UUID.fromString("4f3c2a10-7b6e-4d21-9a8b-1c2d3e4f5a6b");
  private static final long DAY = 24000;

  static Stream<Arguments> rigs() {
    return Stream.of(
        Arguments.of(
            List.of("head_tilt", "idle", "shake", "sit", "sleep", "tail_wag", "walk"),
            List.of("sleep")),
        Arguments.of(List.of("sleep_2", "idle", "sleep"), List.of("sleep", "sleep_2")),
        Arguments.of(
            List.of("sleep_10", "sleep_3", "sleep", "sleep_2"),
            List.of("sleep", "sleep_2", "sleep_3", "sleep_10")),
        Arguments.of(
            List.of(
                "sleep",
                "sleep_0",
                "sleep_1",
                "sleep_",
                "sleep_02",
                "sleepy",
                "sleep_side",
                "sit_sleep"),
            List.of("sleep")),
        Arguments.of(List.of("idle", "walk"), List.of()));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @MethodSource("rigs")
  @DisplayName("only sleep and sleep_<n> clips are sleep variants, in number order")
  void variants(final List<String> clips, final List<String> expected) {
    assertEquals(expected, SleepClipRules.variantsOf(clips));
  }

  static Stream<Arguments> singleClipRigs() {
    return Stream.of(
        Arguments.of(List.of(), 15000L, false),
        Arguments.of(List.of(), 3000L, true),
        Arguments.of(List.of("sleep"), 15000L, false),
        Arguments.of(List.of("sleep"), 3000L, true),
        Arguments.of(List.of("sleep"), Long.MAX_VALUE, false));
  }

  @ParameterizedTest(name = "variants {0}, time {1}, napping {2}")
  @MethodSource("singleClipRigs")
  @DisplayName("a rig with only the original sleep clip, or none, always plays sleep")
  void shippedRigsKeepSleep(final List<String> variants, final long time, final boolean napping) {
    assertEquals("sleep", SleepClipRules.clipFor(variants, DOG, time, false, false, napping));
  }

  @ParameterizedTest(name = "{0} variants")
  @CsvSource({"2", "3", "5"})
  @DisplayName("one dog sleeps in every pose over a month of nights, and keeps it all night")
  void spreadsAcrossNights(final int count) {
    final List<String> variants = variantsUpTo(count);
    final Set<String> seen =
        LongStream.range(0, 30)
            .mapToObj(
                night -> {
                  final String evening =
                      SleepClipRules.clipFor(
                          variants, DOG, night * DAY + 13000, false, false, false);
                  assertEquals(
                      evening,
                      SleepClipRules.clipFor(
                          variants, DOG, night * DAY + 22999, false, false, false));
                  return evening;
                })
            .collect(Collectors.toSet());
    assertEquals(Set.copyOf(variants), seen);
  }

  @ParameterizedTest(name = "{0} variants")
  @CsvSource({"2", "3", "5"})
  @DisplayName("several dogs on the same night do not all pick the same pose")
  void spreadsAcrossDogs(final int count) {
    final List<String> variants = variantsUpTo(count);
    final Set<String> seen =
        IntStream.range(0, 8)
            .mapToObj(n -> new UUID(0x1234L * (n + 1), 0x9876L * (n + 7)))
            .map(dog -> SleepClipRules.clipFor(variants, dog, 15000, false, false, false))
            .collect(Collectors.toSet());
    assertTrue(seen.size() > 1, () -> "every dog picked " + seen);
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
        SleepClipRules.sessionSeed(start, baby, undead, false),
        SleepClipRules.sessionSeed(end, baby, undead, false));
  }

  @ParameterizedTest(name = "baby {0}, undead {1}: {2} then {3}")
  @CsvSource({"false,false,13000,37000", "false,true,0,24000", "true,false,11000,35000"})
  @DisplayName("each new sleep window gets a new seed")
  void seedChangesEachWindow(
      final boolean baby, final boolean undead, final long first, final long next) {
    assertNotEquals(
        SleepClipRules.sessionSeed(first, baby, undead, false),
        SleepClipRules.sessionSeed(next, baby, undead, false));
  }

  @ParameterizedTest(name = "baby {0}, undead {1}: nap from {2} to {3}")
  @CsvSource({
    "false,false,8000,11999",
    "false,false,12000,15600",
    "false,false,20000,23600",
    "true,false,7500,11500",
    "false,true,12100,13500",
    "false,true,22000,23500"
  })
  @DisplayName("a nap keeps one seed even when it runs over a sleep window edge")
  void napSeedHoldsAcrossWindowEdges(
      final boolean baby, final boolean undead, final long start, final long end) {
    assertEquals(
        SleepClipRules.sessionSeed(start, baby, undead, true),
        SleepClipRules.sessionSeed(end, baby, undead, true));
  }

  @ParameterizedTest(name = "nap and bed sleep at {0}")
  @CsvSource({"0", "1", "2", "13000", "23000"})
  @DisplayName("a nap seed never matches a bed sleep seed")
  void napAndBedSeedsDiffer(final long time) {
    assertNotEquals(
        SleepClipRules.sessionSeed(time, false, false, true),
        SleepClipRules.sessionSeed(time, false, false, false));
  }

  @ParameterizedTest(name = "nap at {0} then {1}")
  @CsvSource({"1000,13000", "13000,25000"})
  @DisplayName("naps in different halves of the day get different seeds")
  void napSeedChangesEachPeriod(final long first, final long next) {
    assertNotEquals(
        SleepClipRules.sessionSeed(first, false, false, true),
        SleepClipRules.sessionSeed(next, false, false, true));
  }

  private static List<String> variantsUpTo(final int count) {
    return Stream.concat(
            Stream.of("sleep"), IntStream.rangeClosed(2, count).mapToObj(n -> "sleep_" + n))
        .toList();
  }
}
