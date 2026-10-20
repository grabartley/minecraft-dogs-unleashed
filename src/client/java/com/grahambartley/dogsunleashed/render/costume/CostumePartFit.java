package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.render.rig.SlotAnchor;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class CostumePartFit {

  private CostumePartFit() {}

  public static Matrix4f partToBone(final SlotAnchor anchor, final float rigYawDegrees) {
    final Vector3f centre = anchor.centre();
    final Vector3f size = anchor.size();
    return new Matrix4f()
        .translate(centre)
        .rotate(anchor.restRotation().conjugate())
        .scale(size)
        .rotateY((float) Math.toRadians(rigYawDegrees));
  }
}
