package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.DogCommand;
import net.minecraft.util.math.Vec3d;

public final class ThreatStanceRules {

  public static final double THREAT_RANGE = 8.0;
  public static final double OWNER_RANGE = 16.0;
  public static final int REPATH_TICKS = 10;
  public static final int COWER_EVERY_TICKS = 60;

  private static final double SAME_SPOT_SQUARED = 1.0E-6;

  private ThreatStanceRules() {}

  public static boolean takesStance(
      final DogCommand command,
      final double stanceOffset,
      final boolean hasTarget,
      final boolean sitting) {
    return stanceOffset != 0.0
        && !hasTarget
        && !sitting
        && (command == DogCommand.FOLLOW || command == DogCommand.HEEL);
  }

  public static boolean inRange(final double squaredDistance, final double range) {
    return squaredDistance <= range * range;
  }

  public static Vec3d stancePoint(final Vec3d owner, final Vec3d threat, final double offset) {
    final Vec3d toThreat = new Vec3d(threat.x - owner.x, 0.0, threat.z - owner.z);
    if (toThreat.lengthSquared() < SAME_SPOT_SQUARED) {
      return owner;
    }
    return owner.add(toThreat.normalize().multiply(offset));
  }

  public static boolean cowersNow(final int stanceTicks) {
    return stanceTicks % COWER_EVERY_TICKS == 0;
  }
}
