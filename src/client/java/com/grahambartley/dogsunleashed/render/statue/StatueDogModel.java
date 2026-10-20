package com.grahambartley.dogsunleashed.render.statue;

import com.grahambartley.dogsunleashed.block.StatueMaterial;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;

public final class StatueDogModel extends DogModel {

  private StatueMaterial material = StatueMaterial.STONE;
  private double poseTick;
  private boolean starting;

  void frame(final StatueMaterial material, final double poseTick, final boolean starting) {
    this.material = material;
    this.poseTick = poseTick;
    this.starting = starting;
  }

  @Override
  public Identifier getTextureResource(final UnleashedDogEntity animatable) {
    return StatueCoatTextures.statueOf(super.getTextureResource(animatable), this.material);
  }

  @Override
  public void handleAnimations(
      final UnleashedDogEntity animatable,
      final long instanceId,
      final AnimationState<UnleashedDogEntity> animationState,
      final float partialTick) {
    final AnimatableInstanceCache cache = animatable.getAnimatableInstanceCache();
    final AnimatableManager<UnleashedDogEntity> manager = cache.getManagerForId(instanceId);
    if (this.starting) {
      this.poseAt(animatable, manager, animationState, 0.0);
      this.poseAt(animatable, manager, animationState, 0.0);
    }
    this.poseAt(animatable, manager, animationState, this.poseTick);
    this.setCustomAnimations(animatable, instanceId, animationState);
  }

  private void poseAt(
      final UnleashedDogEntity animatable,
      final AnimatableManager<UnleashedDogEntity> manager,
      final AnimationState<UnleashedDogEntity> animationState,
      final double tick) {
    final AnimationProcessor<UnleashedDogEntity> processor = this.getAnimationProcessor();
    animationState.animationTick = tick;
    processor.preAnimationSetup(animationState, tick);
    if (!processor.getRegisteredBones().isEmpty()) {
      processor.tickAnimation(
          animatable, this, manager, tick, animationState, this.crashIfBoneMissing());
    }
  }
}
