package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;

class HeadTiltRulesTest {

  private static final Animation TILT = clip(DogAnimationKeys.HEAD_TILT, "head", "bone");
  private static final Animation IDLE = clip(DogAnimationKeys.IDLE, "head", "tail");

  @ParameterizedTest(name = "tilts over {0}")
  @ValueSource(strings = {DogAnimationKeys.IDLE, DogAnimationKeys.WALK})
  @DisplayName("a dog that wants to tilt does so over a standing stance")
  void tiltsOverStandingStance(final String stance) {
    assertTrue(HeadTiltRules.tilts(true, clip(stance, "head"), TILT, List.of()));
  }

  @Test
  @DisplayName("a dog with no attention item in sight does not tilt")
  void doesNotTiltWithoutWanting() {
    assertFalse(HeadTiltRules.tilts(false, IDLE, TILT, List.of()));
  }

  @ParameterizedTest(name = "does not tilt over {0}")
  @ValueSource(
      strings = {
        DogAnimationKeys.SIT,
        DogAnimationKeys.SLEEP,
        "sleep_curled",
        DogAnimationKeys.RUN,
        DogAnimationKeys.FLY
      })
  @DisplayName("a dog does not tilt over a stance the tilt was not made for")
  void doesNotTiltOverOtherStances(final String stance) {
    assertFalse(HeadTiltRules.tilts(true, clip(stance, "head", "bone"), TILT, List.of()));
  }

  @Test
  @DisplayName("a dog does not tilt before its base layer has a clip")
  void doesNotTiltWithoutStance() {
    assertFalse(HeadTiltRules.tilts(true, null, TILT, List.of()));
  }

  @Test
  @DisplayName("a rig with no tilt clip never tilts")
  void doesNotTiltWithoutTiltClip() {
    assertFalse(HeadTiltRules.tilts(true, IDLE, null, List.of()));
  }

  @ParameterizedTest(name = "yields to a clip keying {0}")
  @ValueSource(strings = {"head", "bone"})
  @DisplayName("a dog does not tilt while another clip moves any bone the tilt moves")
  void yieldsToClipSharingBones(final String bone) {
    assertFalse(
        HeadTiltRules.tilts(
            true, IDLE, TILT, List.of(clip("tail_wag", "tail"), clip("play_bow", bone, "leg"))));
  }

  @Test
  @DisplayName("a dog still tilts while other clips move only bones the tilt leaves alone")
  void tiltsAlongsideUnrelatedClips() {
    assertTrue(
        HeadTiltRules.tilts(
            true, IDLE, TILT, List.of(clip("tail_wag", "tail"), clip("howl", "neck"))));
  }

  @Test
  @DisplayName("the stance's own bones never block the tilt")
  void ignoresStanceBones() {
    assertTrue(
        HeadTiltRules.tilts(true, clip(DogAnimationKeys.WALK, "head", "bone"), TILT, List.of()));
  }

  private static Animation clip(final String name, final String... bones) {
    return new Animation(
        name,
        1.0,
        Animation.LoopType.LOOP,
        Arrays.stream(bones)
            .map(bone -> new BoneAnimation(bone, null, null, null))
            .toArray(BoneAnimation[]::new),
        null);
  }
}
