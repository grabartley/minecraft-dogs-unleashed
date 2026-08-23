package com.grahambartley.dogsunleashed.render;

import net.minecraft.util.DyeColor;

/**
 * The dye colour applies to the cushion alone, so the planks, shingles and brick around it keep the
 * colours they were painted. GeckoLib carries one tint through a whole draw, and the only seam it
 * can be changed on is a bone boundary, which is why the cushion is authored as its own bone.
 *
 * <p>Tinting is a multiply, so the cushion is painted greyscale on the texture and takes its hue
 * from here. Anything else keeps the tint it inherited from its parent.
 */
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
