package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;

public class PosedDogModel extends DogModel {

  private double poseTick;
  private boolean starting;

  public void frame(final double poseTick, final boolean starting) {
    this.poseTick = poseTick;
    this.starting = starting;
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
