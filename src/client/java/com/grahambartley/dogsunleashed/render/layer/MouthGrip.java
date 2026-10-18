package com.grahambartley.dogsunleashed.render.layer;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Bounds;
import com.grahambartley.dogsunleashed.render.rig.CubeGeometry.Face;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

public final class MouthGrip {

  static final double JAW_HEIGHT_FRACTION = 0.3;
  static final double BITE_DEPTH_FRACTION = 0.1;

  private MouthGrip() {}

  public static Vec3d of(final GeoBone mouth, final double forward) {
    final Bounds muzzle = new Bounds();
    collect(mouth, muzzle);
    if (muzzle.isEmpty()) {
      return Vec3d.ZERO;
    }
    final Vector3f min = muzzle.min();
    final Vector3f max = muzzle.max();
    final double front = forward > 0 ? max.z : min.z;
    final float units = CubeGeometry.MODEL_UNITS_PER_BLOCK;
    return new Vec3d(
        (min.x + max.x) / 2.0 - mouth.getPivotX() / units,
        min.y + (max.y - min.y) * JAW_HEIGHT_FRACTION - mouth.getPivotY() / units,
        front - forward * (max.z - min.z) * BITE_DEPTH_FRACTION - mouth.getPivotZ() / units);
  }

  private static void collect(final GeoBone bone, final Bounds muzzle) {
    for (final List<Face> cube : CubeGeometry.facesByCube(bone)) {
      for (final Face face : cube) {
        for (final Vector3f corner : face.corners()) {
          muzzle.include(corner);
        }
      }
    }
    for (final GeoBone child : bone.getChildBones()) {
      collect(child, muzzle);
    }
  }
}
