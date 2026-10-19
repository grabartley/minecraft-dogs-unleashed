package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.render.rig.CubeGeometry;
import com.grahambartley.dogsunleashed.render.rig.HeadTopLocator;
import com.grahambartley.dogsunleashed.render.rig.RestPose;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import com.grahambartley.dogsunleashed.render.rig.RigProfile.Slot;
import com.grahambartley.dogsunleashed.render.rig.SlotBounds;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public record MobHeadFit(
    String boneName,
    Vector3f base,
    Quaternionf restRotation,
    float edge,
    float depth,
    Set<String> hiddenEars) {

  static final float VANILLA_MARGIN = 1.1875f;

  @Override
  public Vector3f base() {
    return new Vector3f(this.base);
  }

  @Override
  public Quaternionf restRotation() {
    return new Quaternionf(this.restRotation);
  }

  public static Optional<MobHeadFit> of(final BakedGeoModel dog, final RigProfile profile) {
    final List<CubeGeometry.Face> faces = SlotBounds.facesIn(dog, profile.bonesIn(Slot.HEAD));
    if (faces.isEmpty()) {
      return Optional.empty();
    }
    final CubeGeometry.Bounds head = SlotBounds.boundsOf(faces);
    final CubeGeometry.Bounds mouth = SlotBounds.of(dog, profile.bonesIn(Slot.MOUTH));
    if (!mouth.isEmpty()) {
      head.include(mouth.min());
      head.include(mouth.max());
    }
    final float edge = edgeFor(head);
    final float depth = depthFor(head, edge);
    final Vector3f base = baseOf(head, edge);
    final CubeGeometry.Bounds box = boxOf(base, edge, depth);
    final Set<String> hidden = new HashSet<>();
    for (final Slot ear : List.of(Slot.EAR_LEFT, Slot.EAR_RIGHT)) {
      final Set<String> bones = profile.bonesIn(ear);
      if (overlaps(SlotBounds.of(dog, bones), box)) {
        hidden.addAll(bones);
      }
    }
    return HeadTopLocator.locate(dog, profile)
        .map(
            top -> {
              final RestPose pose = RestPose.of(dog.getBone(top.boneName()).orElseThrow());
              return new MobHeadFit(
                  top.boneName(),
                  pose.local(base),
                  pose.rotation(),
                  edge,
                  depth,
                  Set.copyOf(hidden));
            });
  }

  static float edgeFor(final CubeGeometry.Bounds head) {
    final Vector3f size = new Vector3f(head.max()).sub(head.min());
    return Math.max(size.x, size.y) * VANILLA_MARGIN;
  }

  static float depthFor(final CubeGeometry.Bounds head, final float edge) {
    return Math.max(edge, (head.max().z - head.min().z) * VANILLA_MARGIN);
  }

  public float depthStretch() {
    return this.depth / this.edge;
  }

  static Vector3f baseOf(final CubeGeometry.Bounds head, final float edge) {
    final Vector3f centre = head.center();
    return new Vector3f(centre.x, centre.y - edge * 0.5f, centre.z);
  }

  static CubeGeometry.Bounds boxOf(final Vector3f base, final float edge, final float depth) {
    final float half = edge * 0.5f;
    final float halfDepth = depth * 0.5f;
    final CubeGeometry.Bounds box = new CubeGeometry.Bounds();
    box.include(new Vector3f(base.x - half, base.y, base.z - halfDepth));
    box.include(new Vector3f(base.x + half, base.y + edge, base.z + halfDepth));
    return box;
  }

  static boolean overlaps(final CubeGeometry.Bounds a, final CubeGeometry.Bounds b) {
    return !a.isEmpty()
        && !b.isEmpty()
        && a.min().x < b.max().x
        && a.max().x > b.min().x
        && a.min().y < b.max().y
        && a.max().y > b.min().y
        && a.min().z < b.max().z
        && a.max().z > b.min().z;
  }
}
