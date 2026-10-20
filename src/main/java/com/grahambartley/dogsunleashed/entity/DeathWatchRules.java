package com.grahambartley.dogsunleashed.entity;

public final class DeathWatchRules {

  public static final double GATHER_RANGE = 32.0;
  public static final double GUARD_RADIUS = 8.0;
  public static final double RELIEF_RANGE = 8.0;
  public static final double HEARING_RANGE = 48.0;
  public static final double ARRIVAL_DISTANCE = 1.5;
  private static final float BLOCKS_PER_UNIT_VOLUME = 16.0f;

  private DeathWatchRules() {}

  public enum Step {
    KEEP_WATCH,
    CALL_OUT,
    STAND_DOWN
  }

  public record Candidate(
      boolean tamed,
      boolean ownedByDeceased,
      boolean followingOwner,
      boolean alreadyWatching,
      boolean leashed,
      double squaredDistanceToSpot) {}

  public static boolean answersCall(final Candidate dog) {
    return dog.tamed()
        && dog.ownedByDeceased()
        && (dog.followingOwner() || dog.alreadyWatching())
        && !dog.leashed()
        && dog.squaredDistanceToSpot() <= GATHER_RANGE * GATHER_RANGE;
  }

  public static Step next(
      final boolean ownerAliveHere,
      final double ownerSquaredDistanceToSpot,
      final boolean calledOut) {
    if (!ownerAliveHere) {
      return Step.KEEP_WATCH;
    }
    if (ownerSquaredDistanceToSpot <= RELIEF_RANGE * RELIEF_RANGE) {
      return Step.STAND_DOWN;
    }
    if (calledOut || ownerSquaredDistanceToSpot > HEARING_RANGE * HEARING_RANGE) {
      return Step.KEEP_WATCH;
    }
    return Step.CALL_OUT;
  }

  public static float callOutVolume(final float configuredVolume) {
    return configuredVolume <= 0.0f
        ? 0.0f
        : Math.max(configuredVolume, (float) HEARING_RANGE / BLOCKS_PER_UNIT_VOLUME);
  }

  public static boolean hasArrived(final double squaredHorizontalDistanceToSpot) {
    return squaredHorizontalDistanceToSpot <= ARRIVAL_DISTANCE * ARRIVAL_DISTANCE;
  }

  public static boolean isGuardable(final int spotY, final int worldBottomY) {
    return spotY >= worldBottomY;
  }

  public static DogCommand commandAfterWatch(final DogCommand commandBefore) {
    return commandBefore.followsOwnerOnRelocation() ? commandBefore : DogCommand.FOLLOW;
  }
}
