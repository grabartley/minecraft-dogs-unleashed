package com.grahambartley.dogsunleashed.render.sled;

import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

public final class HarnessRing {

  private HarnessRing() {}

  public static Optional<String> boneOf(final BakedGeoModel model, final RigProfile profile) {
    final Set<String> tail = profile.bonesIn(RigProfile.Slot.TAIL);
    return tail.stream()
        .map(model::getBone)
        .flatMap(Optional::stream)
        .filter(bone -> bone.getParent() == null || !tail.contains(bone.getParent().getName()))
        .map(GeoBone::getName)
        .sorted()
        .findFirst();
  }
}
