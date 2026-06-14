package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FetchCarryProfilesTest {
  private static final CarryProfile TENNIS = new CarryProfile(0.10, -0.40, 0.50f);
  private static final CarryProfile STICK = new CarryProfile(0.00, -0.50, 0.40f);
  private static final CarryProfile FRISBEE = new CarryProfile(0.20, -0.30, 1.50f);

  private final FetchCarryProfiles profiles = new FetchCarryProfiles(TENNIS, STICK, FRISBEE);

  @Test
  @DisplayName("forFetchItemPath returns the matching profile for each known fetch item path")
  void forFetchItemPathDispatchesByPath() {
    assertSame(TENNIS, profiles.forFetchItemPath("tennis_ball"));
    assertSame(STICK, profiles.forFetchItemPath("stick"));
    assertSame(FRISBEE, profiles.forFetchItemPath("frisbee"));
  }

  @Test
  @DisplayName("unknown paths fall back to the tennis ball profile")
  void unknownPathFallsBackToTennisBall() {
    assertSame(TENNIS, profiles.forFetchItemPath("ball_of_yarn"));
    assertSame(TENNIS, profiles.forFetchItemPath(""));
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

  @Test
  @DisplayName("constructor rejects null component profiles")
  void rejectsNullProfiles() {
    assertThrows(NullPointerException.class, () -> new FetchCarryProfiles(null, STICK, FRISBEE));
    assertThrows(NullPointerException.class, () -> new FetchCarryProfiles(TENNIS, null, FRISBEE));
    assertThrows(NullPointerException.class, () -> new FetchCarryProfiles(TENNIS, STICK, null));
  }
}
