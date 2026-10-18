package com.grahambartley.dogsunleashed.render.rig;

import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class HeadTopLocator {

  static final float UPWARD = 0.3f;
  static final float EAR_RISE = 0.5f / 16.0f;

  public record HeadTop(
      String boneName,
      Vector3f point,
      Quaternionf restRotation,
      float headWidth,
      Vector3f crown,
      List<RisingEar> risingEars) {

    public Set<String> earsUnder(final float hatWidth) {
      final float half = hatWidth * 0.5f;
      final Set<String> covered = new HashSet<>();
      for (final RisingEar ear : this.risingEars) {
        final CubeGeometry.Bounds bounds = ear.bounds();
        if (bounds.min().x < this.crown.x + half
            && bounds.max().x > this.crown.x - half
            && bounds.min().z < this.crown.z + half
            && bounds.max().z > this.crown.z - half) {
          covered.addAll(ear.bones());
        }
      }
      return Set.copyOf(covered);
    }
  }

  public record RisingEar(Set<String> bones, CubeGeometry.Bounds bounds) {}

  private record Hit(CubeGeometry.Face face, float y) {}

  private HeadTopLocator() {}

  public static Optional<HeadTop> locate(final BakedGeoModel model, final RigProfile profile) {
    final List<CubeGeometry.Face> faces = SlotBounds.facesIn(model, profile.bonesIn(Slot.HEAD));
    if (faces.isEmpty()) {
      return Optional.empty();
    }
    final CubeGeometry.Bounds head = SlotBounds.boundsOf(faces);
    final Vector3f centre = head.center();
    final Optional<Hit> top = surfaceTop(faces, centre.x, centre.z);
    final float topY = top.map(Hit::y).orElse(head.max().y);
    final String boneName =
        top.map(hit -> hit.face().boneName()).orElseGet(() -> highestBone(faces));
    final List<RisingEar> risingEars = new ArrayList<>();
    for (final Slot ear : List.of(Slot.EAR_LEFT, Slot.EAR_RIGHT)) {
      final Set<String> bones = Set.copyOf(profile.bonesIn(ear));
      final CubeGeometry.Bounds bounds = SlotBounds.of(model, bones);
      if (!bounds.isEmpty() && bounds.max().y > topY + EAR_RISE) {
        risingEars.add(new RisingEar(bones, bounds));
      }
    }
    final Vector3f crown = new Vector3f(centre.x, topY, centre.z);
    final RestPose pose = RestPose.of(model.getBone(boneName).orElseThrow());
    return Optional.of(
        new HeadTop(
            boneName,
            pose.local(new Vector3f(crown)),
            pose.rotation(),
            head.max().x - head.min().x,
            crown,
            List.copyOf(risingEars)));
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
