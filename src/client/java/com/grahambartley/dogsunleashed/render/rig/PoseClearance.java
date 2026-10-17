package com.grahambartley.dogsunleashed.render.rig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public final class PoseClearance {

  static final float CLEARANCE = 0.1f / 16.0f;
  static final int MAX_STEPS = 8;

  private static final float MODEL_UNITS_PER_BLOCK = 16.0f;
  private static final float DEGENERATE = 1.0e-9f;

  public record Box(Matrix4f modelToLocal, Vector3f min, Vector3f max) {

    public boolean contains(final Vector3f modelPoint) {
      final Vector3f local = this.modelToLocal.transformPosition(new Vector3f(modelPoint));
      return local.x > this.min.x
          && local.x < this.max.x
          && local.y > this.min.y
          && local.y < this.max.y
          && local.z > this.min.z
          && local.z < this.max.z;
    }

    public float exitAlong(final Vector3f modelPoint, final Vector3f modelDirection) {
      final Vector3f origin = this.modelToLocal.transformPosition(new Vector3f(modelPoint));
      final Vector3f direction = this.modelToLocal.transformDirection(new Vector3f(modelDirection));
      float exit = Float.POSITIVE_INFINITY;
      for (int axis = 0; axis < 3; axis++) {
        final float step = direction.get(axis);
        if (step == 0.0f) {
          continue;
        }
        final float wall = step > 0 ? this.max.get(axis) : this.min.get(axis);
        exit = Math.min(exit, (wall - origin.get(axis)) / step);
      }
      return Float.isInfinite(exit) ? 0.0f : Math.max(0.0f, exit);
    }
  }

  private PoseClearance() {}

  public static List<Box> liveBoxesOf(final BakedGeoModel model, final Set<String> boneNames) {
    final List<Box> boxes = new ArrayList<>();
    collectBoxes(model.topLevelBones(), boneNames, boxes);
    return boxes;
  }

  public static float pushOut(
      final List<Box> boxes, final List<Vector3f> samples, final Vector3f direction) {
    float pushed = 0.0f;
    for (int step = 0; step < MAX_STEPS; step++) {
      float deepest = 0.0f;
      for (final Vector3f sample : samples) {
        final Vector3f moved = new Vector3f(direction).mul(pushed).add(sample);
        for (final Box box : boxes) {
          if (box.contains(moved)) {
            deepest = Math.max(deepest, box.exitAlong(moved, direction));
          }
        }
      }
      if (deepest == 0.0f) {
        return pushed;
      }
      pushed += deepest + CLEARANCE;
    }
    return pushed;
  }

  private static void collectBoxes(
      final List<GeoBone> bones, final Set<String> boneNames, final List<Box> out) {
    for (final GeoBone bone : bones) {
      if (bone.isHidden()) {
        continue;
      }
      if (boneNames.contains(bone.getName())) {
        final Matrix4f boneToModel = LivePose.of(bone);
        for (final GeoCube cube : bone.getCubes()) {
          boxOf(boneToModel, cube).ifPresent(out::add);
        }
      }
      if (!bone.isHidingChildren()) {
        collectBoxes(bone.getChildBones(), boneNames, out);
      }
    }
  }

  static Optional<Box> boxOf(final Matrix4f boneToModel, final GeoCube cube) {
    final Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
    final Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
    for (final GeoQuad quad : cube.quads()) {
      if (quad == null) {
        continue;
      }
      for (final GeoVertex vertex : quad.vertices()) {
        min.min(vertex.position());
        max.max(vertex.position());
      }
    }
    if (min.x > max.x) {
      return Optional.empty();
    }
    final float pivotX = (float) cube.pivot().x / MODEL_UNITS_PER_BLOCK;
    final float pivotY = (float) cube.pivot().y / MODEL_UNITS_PER_BLOCK;
    final float pivotZ = (float) cube.pivot().z / MODEL_UNITS_PER_BLOCK;
    final Matrix4f cubeToModel =
        new Matrix4f(boneToModel)
            .translate(pivotX, pivotY, pivotZ)
            .rotateZ((float) cube.rotation().z)
            .rotateY((float) cube.rotation().y)
            .rotateX((float) cube.rotation().x)
            .translate(-pivotX, -pivotY, -pivotZ);
    if (Math.abs(cubeToModel.determinant()) < DEGENERATE) {
      return Optional.empty();
    }
    return Optional.of(new Box(cubeToModel.invert(), min, max));
  }
}
