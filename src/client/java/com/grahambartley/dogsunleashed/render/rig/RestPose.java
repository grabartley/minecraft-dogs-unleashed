package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.List;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;

public final class RestPose {

  public static final RestPose NONE = new RestPose(List.of());

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;

  public record Joint(Vector3f pivot, Vector3f rotationRadians) {}

  private final List<Joint> childToRoot;

  public RestPose(final List<Joint> childToRoot) {
    this.childToRoot = List.copyOf(childToRoot);
  }

  public static RestPose of(final GeoBone bone) {
    final List<Joint> joints = new ArrayList<>();
    for (GeoBone current = bone; current != null; current = current.getParent()) {
      final BoneSnapshot rest = current.getInitialSnapshot();
      final Vector3f rotation =
          rest == null
              ? new Vector3f(current.getRotX(), current.getRotY(), current.getRotZ())
              : new Vector3f(rest.getRotX(), rest.getRotY(), rest.getRotZ());
      joints.add(
          new Joint(
              new Vector3f(
                  current.getPivotX() / MODEL_UNITS_PER_BLOCK,
                  current.getPivotY() / MODEL_UNITS_PER_BLOCK,
                  current.getPivotZ() / MODEL_UNITS_PER_BLOCK),
              rotation));
    }
    return new RestPose(joints);
  }

  public Vector3f point(final Vector3f local) {
    Vector3f posed = new Vector3f(local);
    for (final Joint joint : this.childToRoot) {
      posed = CubeGeometry.rotateAroundPivot(posed, joint.pivot(), joint.rotationRadians());
    }
    return posed;
  }

  public Vector3f direction(final Vector3f local) {
    return new Vector3f(local).rotate(this.rotation()).normalize();
  }

  public Quaternionf rotation() {
    final Quaternionf total = new Quaternionf();
    for (final Joint joint : this.childToRoot) {
      final Vector3f r = joint.rotationRadians();
      total.premul(new Quaternionf().rotationZ(r.z).rotateY(r.y).rotateX(r.x));
    }
    return total;
  }
}
