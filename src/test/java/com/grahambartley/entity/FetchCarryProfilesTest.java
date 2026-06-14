package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class FetchCarryProfilesTest {

  private static final CarryProfile TENNIS = new CarryProfile(0.10, -0.40, 0.50f);
  private static final CarryProfile STICK = new CarryProfile(0.00, -0.50, 0.40f);
  private static final CarryProfile FRISBEE = new CarryProfile(0.20, -0.30, 1.50f);

  private final FetchCarryProfiles profiles = new FetchCarryProfiles(TENNIS, STICK, FRISBEE);

  static Stream<Arguments> knownPaths() {
    return Stream.of(
        Arguments.of("tennis_ball", TENNIS),
        Arguments.of("stick", STICK),
        Arguments.of("frisbee", FRISBEE));
  }

  @ParameterizedTest(name = "\"{0}\" -> {1}")
  @MethodSource("knownPaths")
  @DisplayName("forFetchItemPath returns the matching profile for each known fetch item path")
  void forFetchItemPathReturnsMatchingProfile(final String path, final CarryProfile expected) {
    assertSame(expected, profiles.forFetchItemPath(path));
  }

  @ParameterizedTest(name = "\"{0}\" falls back to tennis ball")
  @ValueSource(strings = {"ball_of_yarn", "", "TENNIS_BALL", "stick "})
  @DisplayName("unknown paths fall back to the tennis ball profile")
  void unknownPathFallsBackToTennisBall(final String path) {
    assertSame(TENNIS, profiles.forFetchItemPath(path));
  }

  @Test
  @DisplayName("null path falls back to the tennis ball profile")
  void nullPathFallsBackToTennisBall() {
    assertSame(TENNIS, profiles.forFetchItemPath(null));
  }

  @Test
  @DisplayName("null FetchItemType falls back to the tennis ball profile")
  void nullFetchItemFallsBackToTennisBall() {
    assertSame(TENNIS, profiles.forFetchItem(null));
  }

  static Stream<Arguments> nullComponentSlots() {
    final Function<CarryProfile, FetchCarryProfiles> nullTennis =
        ignored -> new FetchCarryProfiles(null, STICK, FRISBEE);
    final Function<CarryProfile, FetchCarryProfiles> nullStick =
        ignored -> new FetchCarryProfiles(TENNIS, null, FRISBEE);
    final Function<CarryProfile, FetchCarryProfiles> nullFrisbee =
        ignored -> new FetchCarryProfiles(TENNIS, STICK, null);
    return Stream.of(
        Arguments.of("tennisBall", nullTennis),
        Arguments.of("stick", nullStick),
        Arguments.of("frisbee", nullFrisbee));
  }

  @ParameterizedTest(name = "null {0}")
  @MethodSource("nullComponentSlots")
  @DisplayName("constructor rejects null in every component slot")
  void constructorRejectsNullComponent(
      final String slot, final Function<CarryProfile, FetchCarryProfiles> factory) {
    assertThrows(NullPointerException.class, () -> factory.apply(null));
  }
}
