package com.grahambartley.dogsunleashed.entity;

public final class DogJumpPhysics {

  public static final double VANILLA_JUMP_STRENGTH = 0.42;
  public static final double VANILLA_CLEARANCE = 1.0;
  public static final double FENCE_HEIGHT = 1.5;
  static final double GRAVITY = 0.08;
  static final double VERTICAL_DRAG = 0.98;
  static final double CLEARANCE_MARGIN = 0.1;
  private static final int SEARCH_STEPS = 40;
  private static final double MAX_STRENGTH = 2.0;

  private DogJumpPhysics() {}

  public static double peakHeight(final double strength) {
    double height = 0.0;
    double velocity = strength;
    while (velocity > 0.0) {
      height += velocity;
      velocity = (velocity - GRAVITY) * VERTICAL_DRAG;
    }
    return height;
  }

  public static double strengthToClear(final double clearance) {
    final double target = clearance + CLEARANCE_MARGIN;
    double low = 0.0;
    double high = MAX_STRENGTH;
    for (int step = 0; step < SEARCH_STEPS; step++) {
      final double middle = (low + high) / 2.0;
      if (peakHeight(middle) < target) {
        low = middle;
      } else {
        high = middle;
      }
    }
    return high;
  }
}
