package com.grahambartley.dogsunleashed.entity.expedition;

import java.util.Optional;

public final class ExpeditionEligibility {

  private static final String PREFIX = "screen.dogs-unleashed.command_wheel.explore.";
  public static final String UNDEAD = PREFIX + "undead";
  public static final String RIDDEN = PREFIX + "ridden";
  public static final String SLEEPING = PREFIX + "sleeping";
  public static final String SITTING = PREFIX + "sitting";
  public static final String LEASHED = PREFIX + "leashed";

  private ExpeditionEligibility() {}

  public static Optional<String> refusalKey(
      final boolean undead,
      final boolean ridden,
      final boolean sleeping,
      final boolean sitting,
      final boolean leashed) {
    if (undead) {
      return Optional.of(UNDEAD);
    }
    if (ridden) {
      return Optional.of(RIDDEN);
    }
    if (sleeping) {
      return Optional.of(SLEEPING);
    }
    if (sitting) {
      return Optional.of(SITTING);
    }
    if (leashed) {
      return Optional.of(LEASHED);
    }
    return Optional.empty();
  }
}
