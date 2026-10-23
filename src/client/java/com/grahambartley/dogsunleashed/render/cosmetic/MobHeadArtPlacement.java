package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.render.rig.RestPose;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class MobHeadArtPlacement {

  private MobHeadArtPlacement() {}

  public static Matrix4f modelToBone(final RestPose pose) {
    final Vector3f origin = pose.local(new Vector3f());
    final Vector3f x = pose.local(new Vector3f(1.0f, 0.0f, 0.0f)).sub(origin);
    final Vector3f y = pose.local(new Vector3f(0.0f, 1.0f, 0.0f)).sub(origin);
    final Vector3f z = pose.local(new Vector3f(0.0f, 0.0f, 1.0f)).sub(origin);
    return new Matrix4f().set(new Matrix3f(x, y, z)).setTranslation(origin);
  }
}
