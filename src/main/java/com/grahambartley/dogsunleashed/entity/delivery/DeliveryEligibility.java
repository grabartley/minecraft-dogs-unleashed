package com.grahambartley.dogsunleashed.entity.delivery;

import java.util.Optional;

public final class DeliveryEligibility {

  private static final String PREFIX = "screen.dogs-unleashed.command_wheel.deliver.";
  public static final String NO_SADDLEBAGS = PREFIX + "no_saddlebags";
  public static final String NO_ONE_ONLINE = PREFIX + "no_one_online";
  public static final String RIDDEN = PREFIX + "ridden";
  public static final String SLEEPING = PREFIX + "sleeping";
  public static final String SITTING = PREFIX + "sitting";
  public static final String LEASHED = PREFIX + "leashed";

  private DeliveryEligibility() {}

  public static Optional<String> refusalKey(
      final boolean wearsSaddlebags,
      final boolean anyoneElseOnline,
      final boolean ridden,
      final boolean sleeping,
      final boolean sitting,
      final boolean leashed) {
    if (!wearsSaddlebags) {
      return Optional.of(NO_SADDLEBAGS);
    }
    if (!anyoneElseOnline) {
      return Optional.of(NO_ONE_ONLINE);
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
