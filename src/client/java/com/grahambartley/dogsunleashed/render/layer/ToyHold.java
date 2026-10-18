package com.grahambartley.dogsunleashed.render.layer;

import net.minecraft.util.math.Vec3d;

public enum ToyHold {
  BALL,
  FRISBEE,
  STICK;

  static final double BALL_RADIUS = 6.0 / 16.0;
  static final double FRISBEE_RADIUS = 8.0 / 16.0;
  static final double FRISBEE_HALF_THICKNESS = 1.0 / 16.0;
  static final float FRISBEE_DROOP_DEGREES = 65.0f;

  public static ToyHold forToy(final String toyPath) {
    return switch (toyPath) {
      case "tennis_ball" -> BALL;
      case "frisbee" -> FRISBEE;
      default -> STICK;
    };
  }

  public Vec3d point(final double forward) {
    return switch (this) {
      case BALL -> new Vec3d(0.0, BALL_RADIUS, 0.0);
      case FRISBEE -> new Vec3d(0.0, FRISBEE_HALF_THICKNESS, -forward * FRISBEE_RADIUS);
      case STICK -> Vec3d.ZERO;
    };
  }

  public float droopDegrees(final double forward) {
    return this == FRISBEE ? (float) (forward * FRISBEE_DROOP_DEGREES) : 0.0f;
  }
}
