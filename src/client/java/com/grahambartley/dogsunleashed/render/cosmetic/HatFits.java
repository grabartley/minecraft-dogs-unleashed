package com.grahambartley.dogsunleashed.render.cosmetic;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.DogCosmetic;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class HatFits {

  private final Map<BakedGeoModel, Map<DogCosmetic, Optional<HatFit>>> fits =
      new MapMaker().weakKeys().makeMap();

  public Optional<HatFit> of(
      final BakedGeoModel dog, final RigProfile profile, final DogCosmetic hat) {
    return this.fits
        .computeIfAbsent(dog, model -> new EnumMap<>(DogCosmetic.class))
        .computeIfAbsent(hat, cosmetic -> HatFit.of(dog, profile, cosmetic));
  }

  public Set<String> earsHiddenOn(
      final UnleashedDogEntity dog, final BakedGeoModel model, final RigProfile profile) {
    return DrawnCosmetic.on(dog)
        .filter(cosmetic -> cosmetic.mount() == DogCosmetic.Mount.HEAD)
        .flatMap(hat -> this.of(model, profile, hat))
        .map(HatFit::hiddenEars)
        .orElse(Set.of());
  }
}
