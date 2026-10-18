package com.grahambartley.dogsunleashed.render.rig;

import com.grahambartley.dogsunleashed.render.rig.CollarBandLocator.CollarSpot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class CharmMount {

  private static final float CHARM_SCALE = 0.25f;

  private static final float CHARM_HALF_DEPTH = 0.5f / 16.0f;
  private static final float HALF_TURN_DEGREES = 180.0f;

  private CharmMount() {}

  public static void place(final MatrixStack poseStack, final CollarSpot spot) {
    poseStack.translate(spot.point().x, spot.point().y, spot.point().z);
    poseStack.multiply(new Quaternionf(spot.restRotation()).conjugate());
    if (!spot.facesNegativeZ()) {
      poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(HALF_TURN_DEGREES));
    }
    poseStack.scale(CHARM_SCALE, CHARM_SCALE, CHARM_SCALE);
    poseStack.translate(0.0f, 0.0f, -CHARM_HALF_DEPTH);
  }

  public static Matrix4f charmToBone(final CollarSpot spot) {
    final MatrixStack placed = new MatrixStack();
    place(placed, spot);
    return new Matrix4f(placed.peek().getPositionMatrix());
  }

  public record Sides(Set<String> body, Set<String> head) {}

  public static Vector3f clearanceInBone(
      final List<PoseClearance.Box> body,
      final List<PoseClearance.Box> shell,
      final Supplier<List<PoseClearance.Box>> head,
      final Matrix4fc boneToModel,
      final Matrix4fc charmToBone,
      final CubeGeometry.Bounds charmBounds,
      final boolean facesNegativeZ) {
    final Matrix4f charmToModel = new Matrix4f(boneToModel).mul(charmToBone);
    final List<Vector3f> front = toModel(charmToModel, faceCentreLine(charmBounds));
    final List<Vector3f> back = toModel(charmToModel, backCentreLine(charmBounds));
    final Vector3f forward = new Vector3f(0.0f, 0.0f, facesNegativeZ ? -1.0f : 1.0f);
    final float pushed =
        Math.max(
            PoseClearance.pushOut(body, front, forward),
            PoseClearance.pushOut(shell, back, forward));
    if (pushed == 0.0f || buriedAfter(head.get(), front, new Vector3f(forward).mul(pushed))) {
      return new Vector3f();
    }
    return new Matrix4f(boneToModel).invert().transformDirection(forward.mul(pushed));
  }

  private static List<Vector3f> toModel(final Matrix4fc charmToModel, final List<Vector3f> points) {
    final List<Vector3f> samples = new ArrayList<>();
    for (final Vector3f point : points) {
      samples.add(charmToModel.transformPosition(new Vector3f(point)));
    }
    return samples;
  }

  private static boolean buriedAfter(
      final List<PoseClearance.Box> boxes, final List<Vector3f> samples, final Vector3f push) {
    for (final Vector3f sample : samples) {
      final Vector3f moved = new Vector3f(sample).add(push);
      for (final PoseClearance.Box box : boxes) {
        if (box.contains(moved)) {
          return true;
        }
      }
    }
    return false;
  }

  public static Sides sidesOf(
      final BakedGeoModel model, final CollarSpot spot, final String mouthBone) {
    final Optional<GeoBone> collar = model.getBone(spot.boneName());
    final Optional<GeoBone> mouth = model.getBone(mouthBone);
    if (collar.isEmpty() || mouth.isEmpty()) {
      return new Sides(Set.of(), Set.of());
    }
    final Map<String, Vector3f> centres = new HashMap<>();
    collectRestCentres(model.topLevelBones(), centres);
    centres.remove(spot.boneName());
    final Set<String> body =
        bodySide(
            centres,
            RestPose.of(collar.get()).point(spot.point()),
            CubeGeometry.posedBoundsOf(mouth.get()).center());
    final Set<String> head = new HashSet<>(centres.keySet());
    head.removeAll(body);
    return new Sides(Set.copyOf(body), Set.copyOf(head));
  }

  static Set<String> bodySide(
      final Map<String, Vector3f> restCentres, final Vector3f anchor, final Vector3f mouth) {
    final Vector3f towardMouth = new Vector3f(mouth).sub(anchor);
    final Set<String> body = new HashSet<>();
    restCentres.forEach(
        (name, centre) -> {
          if (new Vector3f(centre).sub(anchor).dot(towardMouth) < 0.0f) {
            body.add(name);
          }
        });
    return body;
  }

  private static void collectRestCentres(
      final List<GeoBone> bones, final Map<String, Vector3f> out) {
    for (final GeoBone bone : bones) {
      final CubeGeometry.Bounds bounds = CubeGeometry.posedBoundsOf(bone);
      if (!bounds.isEmpty()) {
        out.put(bone.getName(), bounds.center());
      }
      collectRestCentres(bone.getChildBones(), out);
    }
  }

  static List<Vector3f> faceCentreLine(final CubeGeometry.Bounds bounds) {
    final Vector3f min = bounds.min();
    final Vector3f max = bounds.max();
    final float middleX = (min.x + max.x) * 0.5f;
    return List.of(
        new Vector3f(middleX, max.y, min.z),
        new Vector3f(middleX, (min.y + max.y) * 0.5f, min.z),
        new Vector3f(middleX, min.y, min.z));
  }

  static List<Vector3f> backCentreLine(final CubeGeometry.Bounds bounds) {
    final Vector3f min = bounds.min();
    final Vector3f max = bounds.max();
    final float middleX = (min.x + max.x) * 0.5f;
    return List.of(
        new Vector3f(middleX, max.y, max.z),
        new Vector3f(middleX, (min.y + max.y) * 0.5f, max.z),
        new Vector3f(middleX, min.y, max.z));
  }
}
