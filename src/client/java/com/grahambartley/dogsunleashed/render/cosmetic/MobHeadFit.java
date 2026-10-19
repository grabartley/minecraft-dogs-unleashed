package com.grahambartley.dogsunleashed.render.cosmetic;

import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
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
    float depthStretch,
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

  public static Optional<MobHeadFit> of(
      final BakedGeoModel dog, final RigProfile profile, final MobHead head) {
    final CubeGeometry.Bounds muzzle = muzzleOf(dog, profile);
    if (muzzle.isEmpty()) {
      return Optional.empty();
    }
    final float edge = edgeFor(muzzle);
    final float depthStretch = head.stretchesOverTheMuzzle() ? depthStretchFor(muzzle, edge) : 1.0f;
    final Vector3f base = baseOf(muzzle, edge);
    final CubeGeometry.Bounds drawn = MobHeadShape.placed(head, base, edge, depthStretch);
    final Set<String> hidden = new HashSet<>();
    for (final Slot ear : List.of(Slot.EAR_LEFT, Slot.EAR_RIGHT)) {
      final Set<String> bones = profile.bonesIn(ear);
      if (SlotBounds.of(dog, bones).overlaps(drawn)) {
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
                  depthStretch,
                  Set.copyOf(hidden));
            });
  }

  static CubeGeometry.Bounds muzzleOf(final BakedGeoModel dog, final RigProfile profile) {
    final CubeGeometry.Bounds muzzle = SlotBounds.of(dog, profile.bonesIn(Slot.HEAD));
    if (muzzle.isEmpty()) {
      return muzzle;
    }
    final CubeGeometry.Bounds mouth = SlotBounds.of(dog, profile.bonesIn(Slot.MOUTH));
    if (!mouth.isEmpty()) {
      muzzle.include(mouth.min());
      muzzle.include(mouth.max());
    }
    return muzzle;
  }

  static float edgeFor(final CubeGeometry.Bounds head) {
    final Vector3f size = new Vector3f(head.max()).sub(head.min());
    return Math.max(size.x, size.y) * VANILLA_MARGIN;
  }

  static float depthStretchFor(final CubeGeometry.Bounds head, final float edge) {
    return Math.max(1.0f, (head.max().z - head.min().z) * VANILLA_MARGIN / edge);
  }

  static Vector3f baseOf(final CubeGeometry.Bounds head, final float edge) {
    final Vector3f centre = head.center();
    return new Vector3f(centre.x, centre.y - edge * 0.5f, centre.z);
  }
}
