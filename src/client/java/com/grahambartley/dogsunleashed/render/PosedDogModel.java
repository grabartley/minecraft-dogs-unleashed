package com.grahambartley.dogsunleashed.render;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.model.DogModel;
import com.grahambartley.dogsunleashed.render.rig.PrivateRigBones;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;

public class PosedDogModel extends DogModel {

  private final PrivateRigBones rigs;
  private BakedGeoModel posing;
  private double poseTick;
  private boolean starting;

  public PosedDogModel() {
    this(PrivateRigBones.shared());
  }

  PosedDogModel(final PrivateRigBones rigs) {
    this.rigs = rigs;
  }

  @Override
  public BakedGeoModel getBakedModel(final Identifier location) {
    final BakedGeoModel own = this.rigs.of(location);
    if (own != this.posing) {
      this.getAnimationProcessor().setActiveModel(own);
      this.posing = own;
    }
    return own;
  }

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
