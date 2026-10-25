package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.trick.DogTrick;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

public final class DogAnimationControllers {

  private static final int HEAD_TILT_TRANSITION_TICKS = 5;
  private static final int GROWL_TRANSITION_TICKS = 4;
  private static final int SCENT_TRANSITION_TICKS = 4;
  private static final int BONE_DIG_TRANSITION_TICKS = 4;
  private static final int TUG_TRANSITION_TICKS = 4;
  private static final int EAT_TRANSITION_TICKS = 4;

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
    final AnimationController<UnleashedDogEntity> movement =
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
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.SIT));
              }
              if (isGalloping(state.getAnimatable())) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.RUN));
              }
              if (WalkRules.walks(
                  state.getAnimatable().limbAnimator.getSpeed(),
                  playing(state.getController(), DogAnimationKeys.WALK))) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.WALK));
              }
              return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.IDLE));
            });
    controllers.add(movement);
    final List<AnimationController<UnleashedDogEntity>> layers = new ArrayList<>();

    layers.add(
        new AnimationController<>(
                dog,
                "tail",
                0,
                state -> {
                  final UnleashedDogEntity animatable = state.getAnimatable();
                  if (animatable.getAnimationState().getTailWagTimerTicks() > 0
                      && !animatable.getSleepController().isAsleep()) {
                    return state.setAndContinue(
                        RawAnimation.begin().thenLoop(DogAnimationKeys.TAIL_WAG));
                  }
                  return PlayState.STOP;
                })
            .setAnimationSpeedHandler(
                animatable -> animatable.getComponents().get(DogBond.class).step().wagSpeed()));

    layers.add(
        new AnimationController<>(
            dog,
            "shake",
            0,
            state -> {
              if (state.getAnimatable().getAnimationState().isShaking()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(DogAnimationKeys.SHAKE));
              }
              return PlayState.STOP;
            }));

    layers.add(
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

    layers.add(
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

    layers.add(
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

    layers.add(
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

    layers.add(
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

    layers.add(
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

    layers.add(
        new AnimationController<>(dog, DogAnimationKeys.BARK, 0, state -> PlayState.STOP)
            .triggerableAnim(
                DogAnimationKeys.BARK, RawAnimation.begin().thenPlay(DogAnimationKeys.BARK)));

    layers.addAll(poses);
    layers.forEach(controllers::add);

    controllers.add(
        new AnimationController<>(
            dog,
            DogAnimationKeys.HEAD_TILT,
            HEAD_TILT_TRANSITION_TICKS,
            state -> {
              final UnleashedDogEntity animatable = state.getAnimatable();
              if (HeadTiltRules.tilts(
                  animatable.getAnimationState().isHeadTilting(),
                  playingClip(movement),
                  RigClipPresence.find(animatable, DogAnimationKeys.HEAD_TILT),
                  layers.stream()
                      .map(DogAnimationControllers::playingClip)
                      .filter(Objects::nonNull)
                      .toList())) {
                if (state.getController().getAnimationState()
                    == AnimationController.State.STOPPED) {
                  state.getController().forceAnimationReset();
                }
                return state.setAndContinue(
                    RawAnimation.begin().thenPlayAndHold(DogAnimationKeys.HEAD_TILT));
              }
              return PlayState.STOP;
            }));
  }

  private static @Nullable Animation playingClip(
      final AnimationController<UnleashedDogEntity> controller) {
    if (controller.getAnimationState() == AnimationController.State.STOPPED
        || controller.getCurrentAnimation() == null) {
      return null;
    }
    return controller.getCurrentAnimation().animation();
  }

  private static AnimationController<UnleashedDogEntity> pose(
      final UnleashedDogEntity dog, final String key) {
    return new AnimationController<>(dog, key, PoseBlendOut.TICKS, state -> PlayState.STOP)
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
        movement.transitionLength(PoseBlendOut.TICKS);
        movement.forceAnimationReset();
        dog.getAnimationState().poseBlendOut().handBack(dog.getWorld().getTime());
      }
      case SNAP -> movement.transitionLength(0);
      case KEEP -> {}
    }
  }

  private static boolean playing(
      final AnimationController<UnleashedDogEntity> controller, final String clip) {
    final Animation current = playingClip(controller);
    return current != null && clip.equals(current.name());
  }

  static boolean isGalloping(final UnleashedDogEntity dog) {
    final boolean hitched = dog.getComponents().get(DogSledHarness.class).isHitched();
    return GallopRules.gallops(
        dog.getRiding().isRidden(),
        hitched,
        dog.limbAnimator.getSpeed(),
        hitched && RigClipPresence.has(dog, DogAnimationKeys.RUN));
  }
}
