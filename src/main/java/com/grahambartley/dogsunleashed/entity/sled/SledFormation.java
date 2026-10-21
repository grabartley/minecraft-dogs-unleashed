package com.grahambartley.dogsunleashed.entity.sled;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class SledFormation {

  public static final int MAX_TEAM = 6;
  public static final double HITCH_DISTANCE = 1.9;
  public static final double LEAD_TICKS = 3.0;
  public static final double CATCH_UP = 0.25;
  static final double MIN_PAIR_GAP = 1.2;
  static final double LENGTH_PER_WIDTH = 1.8;
  static final double PAIR_SPACING = 0.3;
  static final double MIN_LATERAL = 0.45;
  static final double LATERAL_PER_WIDTH = 0.55;

  private SledFormation() {}

  public record Slot(double forward, double left) {}

  public static Slot slot(final int index, final int teamSize, final double widestDog) {
    final int pair = index / 2;
    final boolean alone = index == teamSize - 1 && teamSize % 2 == 1;
    final double lateral = Math.max(MIN_LATERAL, widestDog * LATERAL_PER_WIDTH);
    final double side = alone ? 0.0 : (index % 2 == 0 ? lateral : -lateral);
    return new Slot(HITCH_DISTANCE + bodyLength(widestDog) / 2.0 + pair * pairGap(widestDog), side);
  }

  public static double pairGap(final double widestDog) {
    return Math.max(MIN_PAIR_GAP, bodyLength(widestDog) + PAIR_SPACING);
  }

  static double bodyLength(final double width) {
    return width * LENGTH_PER_WIDTH;
  }

  public static double restDistance(final int teamSize, final double widestDog) {
    if (teamSize <= 0) {
      return 0.0;
    }
    double total = 0.0;
    for (int index = 0; index < teamSize; index++) {
      total += slot(index, teamSize, widestDog).forward();
    }
    return total / teamSize;
  }

  public static double lead(final double speed) {
    return speed * LEAD_TICKS;
  }

  public static Vec3d forward(final float yawDegrees) {
    final float yaw = yawDegrees * MathHelper.RADIANS_PER_DEGREE;
    return new Vec3d(-MathHelper.sin(yaw), 0.0, MathHelper.cos(yaw));
  }

  public static Vec3d offset(final Slot slot, final float yawDegrees, final double lead) {
    final Vec3d forward = forward(yawDegrees);
    final Vec3d left = new Vec3d(forward.z, 0.0, -forward.x);
    return forward.multiply(slot.forward() + lead).add(left.multiply(slot.left()));
  }

  public static double pull(
      final double distanceToTeam, final double restDistance, final double speed) {
    return Math.max(0.0, Math.min(distanceToTeam - restDistance, speed));
  }
}
