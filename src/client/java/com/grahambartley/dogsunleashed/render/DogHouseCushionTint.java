package com.grahambartley.dogsunleashed.render;

import net.minecraft.util.DyeColor;

public final class DogHouseCushionTint {

  public static final String CUSHION_BONE = "cushion";

  private static final int OPAQUE = 0xFF000000;

  private DogHouseCushionTint() {}

  public static int forBone(final String boneName, final DyeColor color, final int inheritedTint) {
    if (!CUSHION_BONE.equals(boneName)) {
      return inheritedTint;
    }
    return color.getEntityColor() | OPAQUE;
  }
}
