package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;

public final class BringToyRules {

  public static final double SEARCH_RANGE = 8.0;
  public static final double ALREADY_BROUGHT_RANGE = 3.0;
  public static final int LOOK_ONE_IN = 400;

  private BringToyRules() {}

  public static boolean mayLook(
      final DogCommand command,
      final boolean sitting,
      final boolean carrying,
      final boolean hasFetchOrder,
      final double squaredDistanceToOwner) {
    return DogErrandRules.isFree(command, sitting, carrying)
        && !hasFetchOrder
        && DogErrandRules.ownerNearby(squaredDistanceToOwner);
  }

  public static boolean worthBringing(final double squaredDistanceToOwner) {
    return squaredDistanceToOwner > ALREADY_BROUGHT_RANGE * ALREADY_BROUGHT_RANGE;
  }
}
