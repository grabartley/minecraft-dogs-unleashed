package com.grahambartley.dogsunleashed.render.rig;

import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class HeadTopLocator {

  static final float UPWARD = 0.3f;
  static final float EAR_RISE = 0.5f / 16.0f;
  static final float OVERHANG = 0.4f;

  public record HeadTop(
      String boneName,
      Vector3f point,
      Quaternionf restRotation,
      float headWidth,
      float clearWidth) {}

  record Patch(float centreX, float centreZ, float size) {}

  private record Hit(CubeGeometry.Face face, float y) {}

  private HeadTopLocator() {}

  public static Optional<HeadTop> locate(final BakedGeoModel model, final RigProfile profile) {
    final List<CubeGeometry.Face> faces = SlotBounds.facesIn(model, profile.bonesIn(Slot.HEAD));
    if (faces.isEmpty()) {
      return Optional.empty();
    }
    final CubeGeometry.Bounds head = SlotBounds.boundsOf(faces);
    final Optional<Hit> crown = surfaceTop(faces, head.center().x, head.center().z);
    final float crownY = crown.map(Hit::y).orElse(head.max().y);
    final List<CubeGeometry.Bounds> risingEars = new ArrayList<>();
    for (final Slot ear : List.of(Slot.EAR_LEFT, Slot.EAR_RIGHT)) {
      final CubeGeometry.Bounds bounds = SlotBounds.of(model, profile.bonesIn(ear));
      if (!bounds.isEmpty() && bounds.max().y > crownY + EAR_RISE) {
        risingEars.add(bounds);
      }
    }
    final Patch patch = clearPatch(head, risingEars);
    final Optional<Hit> top = surfaceTop(faces, patch.centreX(), patch.centreZ());
    final String boneName =
        top.or(() -> crown).map(hit -> hit.face().boneName()).orElseGet(() -> highestBone(faces));
    final float topY = top.or(() -> crown).map(Hit::y).orElse(head.max().y);
    final RestPose pose = RestPose.of(model.getBone(boneName).orElseThrow());
    final Vector3f point = pose.local(new Vector3f(patch.centreX(), topY, patch.centreZ()));
    return Optional.of(
        new HeadTop(boneName, point, pose.rotation(), head.max().x - head.min().x, patch.size()));
  }

  static Patch clearPatch(
      final CubeGeometry.Bounds head, final List<CubeGeometry.Bounds> risingEars) {
    final Vector3f min = head.min();
    final Vector3f max = head.max();
    final Vector3f centre = head.center();
    final float width = max.x - min.x;
    if (risingEars.isEmpty()) {
      return new Patch(centre.x, centre.z, Math.min(width, max.z - min.z));
    }
    float innerLeft = max.x;
    float innerRight = min.x;
    float earFront = max.z;
    for (final CubeGeometry.Bounds ear : risingEars) {
      if (ear.center().x >= centre.x) {
        innerLeft = Math.min(innerLeft, ear.min().x);
      } else {
        innerRight = Math.max(innerRight, ear.max().x);
      }
      earFront = Math.min(earFront, ear.min().z);
    }
    final float front = Math.min(width, Math.max(0.0f, earFront - min.z) / (1.0f - OVERHANG));
    final float between = Math.min(Math.max(0.0f, innerLeft - innerRight), max.z - min.z);
    Patch best = new Patch(centre.x, earFront - front * 0.5f, front);
    if (between > best.size()) {
      best = new Patch((innerLeft + innerRight) * 0.5f, centre.z, between);
    }
    return best;
  }

  static Optional<Hit> surfaceTop(
      final List<CubeGeometry.Face> faces, final float x, final float z) {
    Hit best = null;
    for (final CubeGeometry.Face face : faces) {
      final Vector3f normal = face.posedNormal();
      if (normal.y < UPWARD) {
        continue;
      }
      final Vector3f[] corners = new Vector3f[face.corners().length];
      for (int i = 0; i < corners.length; i++) {
        corners[i] = face.pose().point(face.corners()[i]);
      }
      if (!coversXz(corners, x, z)) {
        continue;
      }
      final Vector3f origin = corners[0];
      final float y = origin.y - (normal.x * (x - origin.x) + normal.z * (z - origin.z)) / normal.y;
      if (best == null || y > best.y()) {
        best = new Hit(face, y);
      }
    }
    return Optional.ofNullable(best);
  }

  static boolean coversXz(final Vector3f[] corners, final float x, final float z) {
    float sign = 0.0f;
    for (int i = 0; i < corners.length; i++) {
      final Vector3f a = corners[i];
      final Vector3f b = corners[(i + 1) % corners.length];
      final float cross = (b.x - a.x) * (z - a.z) - (b.z - a.z) * (x - a.x);
      if (cross == 0.0f) {
        continue;
      }
      if (sign == 0.0f) {
        sign = Math.signum(cross);
      } else if (Math.signum(cross) != sign) {
        return false;
      }
    }
    return sign != 0.0f;
  }

  private static String highestBone(final List<CubeGeometry.Face> faces) {
    String best = faces.get(0).boneName();
    float bestY = Float.NEGATIVE_INFINITY;
    for (final CubeGeometry.Face face : faces) {
      for (final Vector3f corner : face.corners()) {
        final float y = face.pose().point(corner).y;
        if (y > bestY) {
          bestY = y;
          best = face.boneName();
        }
      }
    }
    return best;
  }
}
