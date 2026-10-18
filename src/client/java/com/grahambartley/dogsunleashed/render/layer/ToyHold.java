package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Bounds;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public enum ToyHold {
  BALL,
  FRISBEE,
  STICK;

  private static final float FRISBEE_DROOP_DEGREES = 65.0f;

  public static ToyHold forToy(final String toyPath) {
    return switch (toyPath) {
      case "tennis_ball" -> BALL;
      case "frisbee" -> FRISBEE;
      case "stick" -> STICK;
      default -> throw new IllegalArgumentException("no carry hold for toy " + toyPath);
    };
  }

  public Vec3d point(final Bounds toy, final double forward) {
    final Vector3f centre = toy.center();
    if (this == FRISBEE) {
      return new Vec3d(centre.x, centre.y, forward > 0 ? toy.min().z : toy.max().z);
    }
    return new Vec3d(centre.x, centre.y, centre.z);
  }

  public float droopDegrees(final double forward) {
    return this == FRISBEE ? (float) (forward * FRISBEE_DROOP_DEGREES) : 0.0f;
  }
}
