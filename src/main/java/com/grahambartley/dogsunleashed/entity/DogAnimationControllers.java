package com.grahambartley.dogsunleashed.entity;

import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public final class DogAnimationControllers {

  private static final double MOVEMENT_THRESHOLD = 0.001;
  private static final float GALLOP_LIMB_SPEED = 0.1f;
  private static final int HEAD_TILT_TRANSITION_TICKS = 5;
  private static final int GROWL_TRANSITION_TICKS = 4;

  private DogAnimationControllers() {}

  public static void register(
      final UnleashedDogEntity dog, final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            dog,
            "movement",
            0,
            state -> {
              if (state.getAnimatable().isSleepingInBed()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.SLEEP));
              }
              if (state.getAnimatable().isInSittingPose()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("sit"));
              }
              if (isGalloping(state.getAnimatable())) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.RUN));
              }
              if (isMoving(state)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
              }
              return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            "tail",
            0,
            state -> {
              final UnleashedDogEntity animatable = state.getAnimatable();
              if (animatable.getAnimationState().getTailWagTimerTicks() > 0
                  && !animatable.isSleepingInBed()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("tail_wag"));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            "shake",
            0,
            state -> {
              if (state.getAnimatable().getAnimationState().isShaking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("shake"));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            "howl",
            0,
            state -> {
              if (dog.getAnimationState().isHowling()) {
                if (dog.isInSittingPose()) {
                  return state.setAndContinue(
                      RawAnimation.begin().thenLoop(DogAnimationKeys.HOWL_SIT));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.HOWL));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            DogAnimationKeys.GROWL,
            GROWL_TRANSITION_TICKS,
            state -> {
              if (dog.getGrowl().isGrowling()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.GROWL));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            "head_tilt",
            HEAD_TILT_TRANSITION_TICKS,
            state -> {
              final UnleashedDogEntity animatable = state.getAnimatable();
              if (animatable.getAnimationState().isHeadTilting()) {
                if (state.getController().getAnimationState()
                    == AnimationController.State.STOPPED) {
                  state.getController().forceAnimationReset();
                }
                return state.setAndContinue(RawAnimation.begin().thenPlayAndHold("head_tilt"));
              }
              return PlayState.STOP;
            }));
  }

  static boolean isGalloping(final UnleashedDogEntity dog) {
    return dog.getRiding().isRidden() && dog.limbAnimator.getSpeed() > GALLOP_LIMB_SPEED;
  }

  static boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared()
        > MOVEMENT_THRESHOLD;
  }
}
