package com.grahambartley.dogsunleashed.entity.goal;

import net.minecraft.util.math.BlockPos;

public final class DigSiteRules {

  static final double REACH = 1.5;

  private DigSiteRules() {}

  public static boolean isBeside(
      final BlockPos ground, final double x, final double y, final double z) {
    final double dx = x - (ground.getX() + 0.5);
    final double dz = z - (ground.getZ() + 0.5);
    final double dy = y - (ground.getY() + 1);
    return Math.hypot(dx, dz) <= REACH && Math.abs(dy) <= REACH;
  }
}
