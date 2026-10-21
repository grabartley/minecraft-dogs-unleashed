package com.grahambartley.dogsunleashed.entity;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class DogAnimationControllers {

  private static final double MOVEMENT_THRESHOLD = 0.001;
  private static final float GALLOP_LIMB_SPEED = 0.1f;
  private static final int HEAD_TILT_TRANSITION_TICKS = 5;
  private static final int GROWL_TRANSITION_TICKS = 4;
  private static final int SCENT_TRANSITION_TICKS = 4;
  private static final int BONE_DIG_TRANSITION_TICKS = 4;

  private static final Map<BakedAnimations, List<String>> SLEEP_VARIANTS =
      Collections.synchronizedMap(new IdentityHashMap<>());

  private DogAnimationControllers() {}

  public static void register(
      final UnleashedDogEntity dog, final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            dog,
            "movement",
            0,
            state -> {
              if (state.getAnimatable().getSleepController().isAsleep()) {
                return state.setAndContinue(
                    RawAnimation.begin().thenLoop(sleepClipOf(state.getAnimatable())));
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
                      && !animatable.getSleepController().isAsleep()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("tail_wag"));
                  }
                  return PlayState.STOP;
                })
            .setAnimationSpeedHandler(
                animatable -> animatable.getComponents().get(DogBond.class).step().wagSpeed()));

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
            "scent",
            SCENT_TRANSITION_TICKS,
            state ->
                switch (dog.getComponents().get(DogScent.class).phase()) {
                  case SNIFFING ->
                      state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.SNIFF));
                  case DIGGING ->
                      state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.DIG));
                  case POINTING, NONE -> PlayState.STOP;
                }));

    controllers.add(
        new AnimationController<>(
            dog,
            "bone_dig",
            BONE_DIG_TRANSITION_TICKS,
            state -> {
              if (dog.getComponents().get(DogBoneBurial.class).isDigging()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.DIG));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(dog, DogAnimationKeys.BARK, 0, state -> PlayState.STOP)
            .triggerableAnim(
                DogAnimationKeys.BARK, RawAnimation.begin().thenPlay(DogAnimationKeys.BARK)));

    controllers.add(
        new AnimationController<>(dog, DogAnimationKeys.PLAY_BOW, 0, state -> PlayState.STOP)
            .triggerableAnim(
                DogAnimationKeys.PLAY_BOW,
                RawAnimation.begin().thenPlay(DogAnimationKeys.PLAY_BOW)));

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

  static String sleepClipOf(final UnleashedDogEntity dog) {
    return SleepClipRules.clipFor(
        sleepVariantsOf(GeckoLibCache.getBakedAnimations(), dog.getRigSourceBreed().animationId()),
        dog.getUuid(),
        dog.getWorld().getTimeOfDay(),
        dog.isBaby(),
        dog.isUndead(),
        dog.getSleepController().isNapping());
  }

  static List<String> sleepVariantsOf(
      final Map<Identifier, BakedAnimations> loadedAnimations, final Identifier rigAnimations) {
    final BakedAnimations rigClips = loadedAnimations.get(rigAnimations);
    if (rigClips == null) {
      return List.of();
    }
    return SLEEP_VARIANTS.computeIfAbsent(
        rigClips, clips -> SleepClipRules.variantsOf(clips.animations().keySet()));
  }

  static boolean isGalloping(final UnleashedDogEntity dog) {
    return dog.getRiding().isRidden() && dog.limbAnimator.getSpeed() > GALLOP_LIMB_SPEED;
  }

  static boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared()
        > MOVEMENT_THRESHOLD;
  }
}
