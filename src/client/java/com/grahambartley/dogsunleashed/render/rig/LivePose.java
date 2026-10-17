package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayDeque;
import java.util.Deque;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;

public final class LivePose {

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private LivePose() {}

  public static Matrix4f of(final GeoBone bone) {
    final Deque<GeoBone> rootToBone = new ArrayDeque<>();
    for (GeoBone current = bone; current != null; current = current.getParent()) {
      rootToBone.push(current);
    }
    final Matrix4f matrix = new Matrix4f();
    for (final GeoBone current : rootToBone) {
      final float pivotX = current.getPivotX() / MODEL_UNITS_PER_BLOCK;
      final float pivotY = current.getPivotY() / MODEL_UNITS_PER_BLOCK;
      final float pivotZ = current.getPivotZ() / MODEL_UNITS_PER_BLOCK;
      matrix
          .translate(
              -current.getPosX() / MODEL_UNITS_PER_BLOCK,
              current.getPosY() / MODEL_UNITS_PER_BLOCK,
              current.getPosZ() / MODEL_UNITS_PER_BLOCK)
          .translate(pivotX, pivotY, pivotZ)
          .rotateZ(current.getRotZ())
          .rotateY(current.getRotY())
          .rotateX(current.getRotX())
          .scale(current.getScaleX(), current.getScaleY(), current.getScaleZ())
          .translate(-pivotX, -pivotY, -pivotZ);
    }
    return matrix;
  }
}
