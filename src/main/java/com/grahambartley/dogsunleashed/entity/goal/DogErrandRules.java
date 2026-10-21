package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;

public final class DogErrandRules {

  public static final double OWNER_RANGE = 16.0;

  private DogErrandRules() {}

  public static boolean isFree(
      final DogCommand command, final boolean sitting, final boolean carrying) {
    return (command == DogCommand.FOLLOW || command == DogCommand.FREE_ROAM)
        && !sitting
        && !carrying;
  }

  public static boolean ownerNearby(final double squaredDistanceToOwner) {
    return squaredDistanceToOwner <= OWNER_RANGE * OWNER_RANGE;
  }
}
