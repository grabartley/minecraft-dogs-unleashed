package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.constant.DataTickets;

public final class CostumePartsAnimator {

  private static final float NO_LIMB_SWING = 0f;

  private final Map<Identifier, CostumePartsModel> models = new HashMap<>();

  public void animate(
      final UnleashedDogEntity dog, final CostumeAssets assets, final float partialTick) {
    if (!GeckoLibCache.getBakedAnimations().containsKey(assets.partsAnimation())) {
      return;
    }
    final CostumePartsModel model =
        this.models.computeIfAbsent(assets.partsModel(), id -> new CostumePartsModel(assets));
    model.getBakedModel(assets.partsModel());
    final AnimationState<UnleashedDogEntity> state =
        new AnimationState<>(dog, NO_LIMB_SWING, NO_LIMB_SWING, partialTick, false);
    state.setData(DataTickets.TICK, dog.getTick(dog));
    state.setData(DataTickets.ENTITY, dog);
    model.handleAnimations(
        dog, CostumeAnimationInstance.idOf(dog.getId(), assets.partsModel()), state, partialTick);
  }
}
