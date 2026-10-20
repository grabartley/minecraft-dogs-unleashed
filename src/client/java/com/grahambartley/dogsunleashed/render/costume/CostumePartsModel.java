package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;

public class CostumePartsModel extends GeoModel<UnleashedDogEntity> {

  private static final boolean CRASH_IF_BONE_MISSING = false;

  private final CostumeAssets assets;

  public CostumePartsModel(final CostumeAssets assets) {
    this.assets = assets;
  }

  @Override
  public Identifier getModelResource(final UnleashedDogEntity animatable) {
    return this.assets.partsModel();
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    return this.assets.partsTexture();
  }

  @Override
  public Identifier getAnimationResource(final UnleashedDogEntity animatable) {
    return this.assets.partsAnimation();
  }

  public BakedGeoModel animate(
      final UnleashedDogEntity dog,
      final AnimatableManager<UnleashedDogEntity> manager,
      final AnimationState<UnleashedDogEntity> state,
      final double frameTime) {
    final BakedGeoModel parts = this.getBakedModel(this.assets.partsModel());
    if (manager.getFirstTickTime() == -1) {
      manager.startedAt(frameTime);
    }
    if (!manager.isFirstTick() && frameTime == manager.getLastUpdateTime()) {
      return parts;
    }
    if (!MinecraftClient.getInstance().isPaused()) {
      manager.updatedAt(frameTime);
    }
    final double animTime = manager.getLastUpdateTime();
    state.animationTick = animTime;
    final AnimationProcessor<UnleashedDogEntity> processor = this.getAnimationProcessor();
    processor.preAnimationSetup(state, animTime);
    if (!processor.getRegisteredBones().isEmpty()) {
      processor.tickAnimation(dog, this, manager, animTime, state, CRASH_IF_BONE_MISSING);
    }
    return parts;
  }
}
