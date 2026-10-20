package com.grahambartley.dogsunleashed.entity;

import net.minecraft.util.math.MathHelper;

public final class Headings {

  private Headings() {}

  public static float yawToward(
      final double fromX, final double fromZ, final double toX, final double toZ) {
    return (float) (MathHelper.atan2(toZ - fromZ, toX - fromX) * MathHelper.DEGREES_PER_RADIAN)
        - 90.0F;
  }
}
