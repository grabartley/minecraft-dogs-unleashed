package com.grahambartley.dogsunleashed.render.costume;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public final class CostumeMotionControllers {

  private static final int TRANSITION_TICKS = 2;

  private CostumeMotionControllers() {}

  public static AnimatableManager<UnleashedDogEntity> addTo(
      final AnimatableManager<UnleashedDogEntity> manager, final UnleashedDogEntity dog) {
    manager.addController(
        new AnimationController<>(
            dog,
            "costume_motion",
            TRANSITION_TICKS,
            state ->
                CostumeMotion.bodyClip(dog.isOnGround(), dog.isTouchingWater(), dog.hasVehicle())
                    .map(clip -> state.setAndContinue(RawAnimation.begin().thenLoop(clip)))
                    .orElse(PlayState.STOP)));
    manager.addController(
        new AnimationController<>(
            dog,
            "costume_bark",
            0,
            state ->
                CostumeMotion.barking(dog.getVocalization().ticksSinceBark(state.getPartialTick()))
                    ? state.setAndContinue(RawAnimation.begin().thenLoop(CostumeMotion.BARK))
                    : PlayState.STOP));
    return manager;
  }
}
