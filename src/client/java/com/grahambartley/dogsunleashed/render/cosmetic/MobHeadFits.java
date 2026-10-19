package com.grahambartley.dogsunleashed.render.cosmetic;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.DogEquipmentSlot;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.cosmetic.MobHead;
import com.grahambartley.dogsunleashed.render.rig.RigProfile;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public final class MobHeadFits {

  private final Map<BakedGeoModel, Optional<MobHeadFit>> fits = new MapMaker().weakKeys().makeMap();

  public static Optional<MobHead> drawnOn(final UnleashedDogEntity dog) {
    return dog.isInvisible()
        ? Optional.empty()
        : MobHead.of(dog.getEquipmentHolder().getStack(DogEquipmentSlot.COSMETIC));
  }

  public Optional<MobHeadFit> of(final BakedGeoModel dog, final RigProfile profile) {
    return this.fits.computeIfAbsent(dog, model -> MobHeadFit.of(model, profile));
  }

  public Set<String> earsHiddenOn(
      final UnleashedDogEntity dog, final BakedGeoModel model, final RigProfile profile) {
    return this.earsHiddenBy(drawnOn(dog), model, profile);
  }

  Set<String> earsHiddenBy(
      final Optional<MobHead> drawn, final BakedGeoModel model, final RigProfile profile) {
    return drawn
        .flatMap(head -> this.of(model, profile))
        .map(MobHeadFit::hiddenEars)
        .orElse(Set.of());
  }
}
