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
    int quarterTurns,
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
    final CubeGeometry.Bounds skull = SlotBounds.of(dog, profile.bonesIn(Slot.HEAD));
    final CubeGeometry.Bounds muzzle = muzzleOf(dog, profile);
    if (muzzle.isEmpty()) {
      return Optional.empty();
    }
    final int quarterTurns = quarterTurnsToward(skull.center(), muzzle.center());
    final Vector3f facing = facedSize(muzzle, quarterTurns);
    final float edge = Math.max(facing.x, facing.y) * VANILLA_MARGIN;
    final float depthStretch =
        head.stretchesOverTheMuzzle() ? Math.max(1.0f, facing.z * VANILLA_MARGIN / edge) : 1.0f;
    final Vector3f base = baseOf(muzzle, edge);
    final CubeGeometry.Bounds drawn =
        turned(MobHeadShape.placed(head, new Vector3f(), edge, depthStretch), quarterTurns, base);
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
                  quarterTurns,
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

  public float facingYaw() {
    return this.quarterTurns * (float) (Math.PI / 2.0);
  }

  static int quarterTurnsToward(final Vector3f skull, final Vector3f muzzle) {
    final Vector3f ahead = new Vector3f(muzzle).sub(skull);
    if (Math.abs(ahead.x) > Math.abs(ahead.z)) {
      return ahead.x < 0.0f ? 1 : 3;
    }
    return ahead.z > 0.0f ? 2 : 0;
  }

  static Vector3f facedSize(final CubeGeometry.Bounds muzzle, final int quarterTurns) {
    final Vector3f size = new Vector3f(muzzle.max()).sub(muzzle.min());
    return quarterTurns % 2 == 0 ? size : new Vector3f(size.z, size.y, size.x);
  }

  static CubeGeometry.Bounds turned(
      final CubeGeometry.Bounds box, final int quarterTurns, final Vector3f offset) {
    final CubeGeometry.Bounds turned = new CubeGeometry.Bounds();
    for (final Vector3f corner : List.of(box.min(), box.max())) {
      turned.include(corner.rotateY(quarterTurns * (float) (Math.PI / 2.0)).add(offset));
    }
    return turned;
  }

  static Vector3f baseOf(final CubeGeometry.Bounds head, final float edge) {
    final Vector3f centre = head.center();
    return new Vector3f(centre.x, centre.y - edge * 0.5f, centre.z);
  }
}
