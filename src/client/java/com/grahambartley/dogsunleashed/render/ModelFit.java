package com.grahambartley.dogsunleashed.render;

import org.joml.Vector3f;

public record ModelFit(float scale, Vector3f offset) {

  public static final float BLOCK_DIAGONAL = (float) Math.sqrt(2.0);

  public ModelFit {
    offset = new Vector3f(offset);
  }

  public static ModelFit intoBlock(final Vector3f min, final Vector3f max) {
    final float longest = Math.max(max.x - min.x, Math.max(max.y - min.y, max.z - min.z));
    final float scale = longest > 0.0f ? 1.0f / longest : 1.0f;
    return new ModelFit(
        scale,
        new Vector3f(
            0.5f - (min.x + max.x) / 2.0f * scale,
            -min.y * scale,
            0.5f - (min.z + max.z) / 2.0f * scale));
  }

  public static ModelFit centredInBlock(final Vector3f min, final Vector3f max) {
    final float longest = Math.max(max.x - min.x, Math.max(max.y - min.y, max.z - min.z));
    return centred(min, max, longest > 0.0f ? 1.0f / longest : 1.0f);
  }

  public static ModelFit acrossBlock(final Vector3f min, final Vector3f max) {
    final float longest = Math.max(max.x - min.x, Math.max(max.y - min.y, max.z - min.z));
    return centred(min, max, longest > 0.0f ? BLOCK_DIAGONAL / longest : 1.0f);
  }

  private static ModelFit centred(final Vector3f min, final Vector3f max, final float scale) {
    return new ModelFit(
        scale,
        new Vector3f(
            0.5f - (min.x + max.x) / 2.0f * scale,
            0.5f - (min.y + max.y) / 2.0f * scale,
            0.5f - (min.z + max.z) / 2.0f * scale));
  }

  @Override
  public Vector3f offset() {
    return new Vector3f(this.offset);
  }
}
