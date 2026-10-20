package com.grahambartley.dogsunleashed.render.costume;

import com.google.common.collect.MapMaker;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;

public final class CostumePartsAnimator {

  private static final float NO_LIMB_SWING = 0f;

  private final Map<Identifier, CostumePartsModel> models = new HashMap<>();
  private final Map<UnleashedDogEntity, Map<Identifier, AnimatableManager<UnleashedDogEntity>>>
      managers = new MapMaker().weakKeys().makeMap();

  public BakedGeoModel animate(
      final UnleashedDogEntity dog,
      final CostumeAssets assets,
      final BakedGeoModel parts,
      final float partialTick) {
    if (!GeckoLibCache.getBakedAnimations().containsKey(assets.partsAnimation())) {
      return parts;
    }
    final CostumePartsModel model =
        this.models.computeIfAbsent(assets.partsModel(), id -> new CostumePartsModel(assets));
    final AnimatableManager<UnleashedDogEntity> manager =
        this.managers
            .computeIfAbsent(dog, key -> new HashMap<>())
            .computeIfAbsent(assets.partsModel(), id -> new AnimatableManager<>(dog));
    final AnimationState<UnleashedDogEntity> state =
        new AnimationState<>(dog, NO_LIMB_SWING, NO_LIMB_SWING, partialTick, false);
    state.setData(DataTickets.TICK, dog.getTick(dog));
    state.setData(DataTickets.ENTITY, dog);
    return model.animate(dog, manager, state, dog.age + partialTick);
  }
}
