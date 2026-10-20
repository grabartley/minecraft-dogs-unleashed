package com.grahambartley.dogsunleashed.render.cosmetic;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class MobHeadFits {

  private final Map<BakedGeoModel, Map<MobHead, Optional<MobHeadFit>>> fits =
      new MapMaker().weakKeys().makeMap();

  public Optional<MobHeadFit> of(
      final BakedGeoModel dog, final RigProfile profile, final MobHead head) {
    return this.fits
        .computeIfAbsent(dog, model -> new EnumMap<>(MobHead.class))
        .computeIfAbsent(head, worn -> MobHeadFit.of(dog, profile, worn));
  }

  public Set<String> earsHiddenOn(
      final UnleashedDogEntity dog, final BakedGeoModel model, final RigProfile profile) {
    return this.earsHiddenBy(DrawnCosmetic.mobHeadOn(dog), model, profile);
  }

  Set<String> earsHiddenBy(
      final Optional<MobHead> drawn, final BakedGeoModel model, final RigProfile profile) {
    return drawn
        .flatMap(head -> this.of(model, profile, head))
        .map(MobHeadFit::hiddenEars)
        .orElse(Set.of());
  }
}
