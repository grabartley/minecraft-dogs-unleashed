package com.grahambartley.dogsunleashed.entity;

public final class DeathWatchRules {

  public static final double GATHER_RANGE = 32.0;
  public static final double GUARD_RADIUS = 8.0;
  public static final double RELIEF_RANGE = 8.0;
  public static final double ARRIVAL_DISTANCE = 1.5;

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
    return calledOut ? Step.KEEP_WATCH : Step.CALL_OUT;
  }

  public static boolean hasArrived(final double squaredDistanceToSpot) {
    return squaredDistanceToSpot <= ARRIVAL_DISTANCE * ARRIVAL_DISTANCE;
  }

  public static DogCommand commandAfterWatch(final DogCommand commandBefore) {
    return commandBefore.followsOwnerOnRelocation() ? commandBefore : DogCommand.FOLLOW;
  }
}
