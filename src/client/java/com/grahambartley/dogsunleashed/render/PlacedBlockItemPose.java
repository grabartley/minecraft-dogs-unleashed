package com.grahambartley.dogsunleashed.render;

import net.minecraft.client.util.math.MatrixStack;

public final class PlacedBlockItemPose {

  private static final double BLOCK_CENTRE = 0.5;

  private PlacedBlockItemPose() {}

  public static void apply(final MatrixStack stack, final float placedScale) {
    stack.translate(BLOCK_CENTRE, 0.0, BLOCK_CENTRE);
    stack.scale(placedScale, placedScale, placedScale);
  }
}
