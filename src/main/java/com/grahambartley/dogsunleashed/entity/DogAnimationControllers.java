package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import java.util.ArrayList;
import java.util.List;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public final class DogAnimationControllers {

  private static final double MOVEMENT_THRESHOLD = 0.001;
  private static final int HEAD_TILT_TRANSITION_TICKS = 5;
  private static final int GROWL_TRANSITION_TICKS = 4;
  private static final int SCENT_TRANSITION_TICKS = 4;
  private static final int BONE_DIG_TRANSITION_TICKS = 4;
  private static final int TUG_TRANSITION_TICKS = 4;
  private static final int EAT_TRANSITION_TICKS = 4;
  private static final int POSE_BLEND_TICKS = 5;
  private static final int BONE_RESET_TICKS = 1;

  private DogAnimationControllers() {}

  public static void register(
      final UnleashedDogEntity dog, final AnimatableManager.ControllerRegistrar controllers) {
    final SleepClipLatch sleepClip = new SleepClipLatch();
    final List<AnimationController<UnleashedDogEntity>> poses = new ArrayList<>();
    poses.add(pose(dog, DogAnimationKeys.PLAY_BOW));
    poses.add(pose(dog, DogAnimationKeys.COWER));
    for (final DogTrick trick : DogTrick.values()) {
      poses.add(pose(dog, trick.animationKey()));
    }
    controllers.add(
        new AnimationController<>(
            dog,
            "movement",
            0,
            state -> {
              blendOutOfPoses(dog, state.getController(), poses);
              if (state.getAnimatable().getSleepController().isAsleep()) {
                return state.setAndContinue(
                    RawAnimation.begin()
                        .thenLoop(
                            sleepClip.hold(
                                state.getAnimatable().getWorld().getTime(),
                                () -> RigSleepClips.shared().clipOf(state.getAnimatable()))));
              }
              sleepClip.release();
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
        new AnimationController<>(
            dog,
            DogAnimationKeys.TUG,
            TUG_TRANSITION_TICKS,
            state -> {
              if (dog.getComponents().get(DogTug.class).isBeingTugged()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.TUG));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(
            dog,
            DogAnimationKeys.EAT,
            EAT_TRANSITION_TICKS,
            state -> {
              if (dog.getComponents().get(DogBowlMeal.class).isEating()
                  && RigClipPresence.has(dog, DogAnimationKeys.EAT)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.EAT));
              }
              return PlayState.STOP;
            }));

    controllers.add(
        new AnimationController<>(dog, DogAnimationKeys.BARK, 0, state -> PlayState.STOP)
            .triggerableAnim(
                DogAnimationKeys.BARK, RawAnimation.begin().thenPlay(DogAnimationKeys.BARK)));

    poses.forEach(controllers::add);

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

  private static AnimationController<UnleashedDogEntity> pose(
      final UnleashedDogEntity dog, final String key) {
    return new AnimationController<>(dog, key, POSE_BLEND_TICKS, state -> PlayState.STOP)
        .triggerableAnim(key, RawAnimation.begin().thenPlayAndHold(key));
  }

  private static void blendOutOfPoses(
      final UnleashedDogEntity dog,
      final AnimationController<UnleashedDogEntity> movement,
      final List<AnimationController<UnleashedDogEntity>> poses) {
    final List<AnimationController<UnleashedDogEntity>> held =
        poses.stream()
            .filter(pose -> pose.getAnimationState() == AnimationController.State.PAUSED)
            .toList();
    switch (PoseBlendOut.step(
        !held.isEmpty(), movement.getAnimationState() == AnimationController.State.RUNNING)) {
      case BLEND_BACK -> {
        held.forEach(pose -> dog.stopTriggeredAnim(pose.getName(), null));
        movement.transitionLength(POSE_BLEND_TICKS);
        movement.forceAnimationReset();
        dog.getAnimationState().setBoneResetTicks(POSE_BLEND_TICKS);
      }
      case SNAP -> {
        movement.transitionLength(0);
        dog.getAnimationState().setBoneResetTicks(BONE_RESET_TICKS);
      }
      case KEEP -> {}
    }
  }

  static boolean isGalloping(final UnleashedDogEntity dog) {
    final boolean hitched = dog.getComponents().get(DogSledHarness.class).isHitched();
    return GallopRules.gallops(
        dog.getRiding().isRidden(),
        hitched,
        dog.limbAnimator.getSpeed(),
        hitched && RigClipPresence.has(dog, DogAnimationKeys.RUN));
  }

  static boolean isMoving(final AnimationState<UnleashedDogEntity> animationState) {
    return animationState.getAnimatable().getVelocity().horizontalLengthSquared()
        > MOVEMENT_THRESHOLD;
  }
}
