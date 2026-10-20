package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;

public final class CostumePartsAnimator {

  private static final float NO_LIMB_SWING = 0f;
  private static final CostumePartsAnimator SHARED = new CostumePartsAnimator();

  private final Map<Identifier, CostumePartsModel> models = new HashMap<>();
  private final Map<UnleashedDogEntity, Map<Identifier, AnimatableManager<UnleashedDogEntity>>>
      managers = new HashMap<>();

  private CostumePartsAnimator() {}

  public static CostumePartsAnimator shared() {
    return SHARED;
  }

  public static void clearOnDisconnect() {
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SHARED.managers.clear());
  }

  public BakedGeoModel animate(
      final UnleashedDogEntity dog,
      final CostumeAssets assets,
      final BakedGeoModel parts,
      final float partialTick) {
    if (!GeckoLibCache.getBakedAnimations().containsKey(assets.partsAnimation())) {
      return parts;
    }
    this.managers.keySet().removeIf(Entity::isRemoved);
    final CostumePartsModel model =
        this.models.computeIfAbsent(assets.partsModel(), id -> new CostumePartsModel(assets));
    final AnimatableManager<UnleashedDogEntity> manager =
        this.managers
            .computeIfAbsent(dog, key -> new HashMap<>())
            .computeIfAbsent(assets.partsModel(), id -> new AnimatableManager<>(dog));
    final double tick = dog.getTick(dog);
    final AnimationState<UnleashedDogEntity> state =
        new AnimationState<>(dog, NO_LIMB_SWING, NO_LIMB_SWING, partialTick, false);
    state.setData(DataTickets.TICK, tick);
    state.setData(DataTickets.ENTITY, dog);
    return model.animate(dog, manager, state, tick + partialTick);
  }
}
