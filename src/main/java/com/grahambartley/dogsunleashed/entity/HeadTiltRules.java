package com.grahambartley.dogsunleashed.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;

public final class HeadTiltRules {

  static final Set<String> TILTABLE_STANCES = Set.of(DogAnimationKeys.IDLE, DogAnimationKeys.WALK);

  private HeadTiltRules() {}

  public static boolean tilts(
      final boolean wantsTilt,
      final @Nullable Animation stance,
      final @Nullable Animation tilt,
      final Collection<Animation> otherPlayingClips) {
    if (!wantsTilt || stance == null || tilt == null) {
      return false;
    }
    if (!TILTABLE_STANCES.contains(stance.name())) {
      return false;
    }
    final Set<String> tiltBones = bonesOf(tilt);
    return otherPlayingClips.stream()
        .noneMatch(clip -> bonesOf(clip).stream().anyMatch(tiltBones::contains));
  }

  private static Set<String> bonesOf(final Animation clip) {
    return Arrays.stream(clip.boneAnimations())
        .map(BoneAnimation::boneName)
        .collect(Collectors.toSet());
  }
}
