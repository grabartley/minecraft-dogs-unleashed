package com.grahambartley.dogsunleashed.render;

import org.joml.Matrix4f;

public final class PlacedBlockItemPose {

  private static final float BLOCK_CENTRE = 0.5f;

  private PlacedBlockItemPose() {}

  public static Matrix4f of(final float placedScale) {
    return new Matrix4f().translate(BLOCK_CENTRE, 0.0f, BLOCK_CENTRE).scale(placedScale);
  }
}
