package com.grahambartley.dogsunleashed.render.layer;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public final class MouthGrip {

  static final double JAW_HEIGHT_FRACTION = 0.3;
  static final double BITE_DEPTH_FRACTION = 0.1;
  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  private static final Map<GeoBone, Vec3d> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

  private MouthGrip() {}

  public static Vec3d of(final GeoBone mouth, final double forward) {
    return CACHE.computeIfAbsent(mouth, bone -> measure(bone, forward));
  }

  static Vec3d measure(final GeoBone mouth, final double forward) {
    final double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
    final double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
    collect(mouth, min, max);
    if (min[0] > max[0]) {
      return Vec3d.ZERO;
    }
    final double depth = max[2] - min[2];
    final double front = forward > 0 ? max[2] : min[2];
    return new Vec3d(
        (min[0] + max[0]) / 2 - mouth.getPivotX() / MODEL_UNITS_PER_BLOCK,
        min[1]
            + (max[1] - min[1]) * JAW_HEIGHT_FRACTION
            - mouth.getPivotY() / MODEL_UNITS_PER_BLOCK,
        front - forward * depth * BITE_DEPTH_FRACTION - mouth.getPivotZ() / MODEL_UNITS_PER_BLOCK);
  }

  private static void collect(final GeoBone bone, final double[] min, final double[] max) {
    for (final GeoCube cube : bone.getCubes()) {
      for (final GeoQuad quad : cube.quads()) {
        if (quad == null) {
          continue;
        }
        for (final GeoVertex vertex : quad.vertices()) {
          final double[] p = {vertex.position().x, vertex.position().y, vertex.position().z};
          for (int axis = 0; axis < 3; axis++) {
            min[axis] = Math.min(min[axis], p[axis]);
            max[axis] = Math.max(max[axis], p[axis]);
          }
        }
      }
    }
    for (final GeoBone child : bone.getChildBones()) {
      collect(child, min, max);
    }
  }
}
