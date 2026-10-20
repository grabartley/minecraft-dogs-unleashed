package com.grahambartley.dogsunleashed.entity.goal;

public final class PointedFetchRules {

  public enum Step {
    GIVE_UP,
    PICK_UP,
    MOVE
  }

  static final double PICKUP_DISTANCE = 1.5;
  static final int MAX_FETCH_TICKS = 600;

  private PointedFetchRules() {}

  public static Step step(
      final int fetchTicks, final boolean cannotPickup, final double squaredDistance) {
    if (fetchTicks > MAX_FETCH_TICKS) {
      return Step.GIVE_UP;
    }
    return !cannotPickup && squaredDistance <= PICKUP_DISTANCE * PICKUP_DISTANCE
        ? Step.PICK_UP
        : Step.MOVE;
  }
}
