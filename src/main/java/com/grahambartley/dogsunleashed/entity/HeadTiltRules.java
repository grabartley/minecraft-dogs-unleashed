package com.grahambartley.dogsunleashed.entity;

import java.util.Collection;
import java.util.Set;
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
    final BoneAnimation[] tiltBones = tilt.boneAnimations();
    return otherPlayingClips.stream().noneMatch(clip -> sharesBone(clip, tiltBones));
  }

  private static boolean sharesBone(final Animation clip, final BoneAnimation[] tiltBones) {
    for (final BoneAnimation bone : clip.boneAnimations()) {
      for (final BoneAnimation tiltBone : tiltBones) {
        if (bone.boneName().equals(tiltBone.boneName())) {
          return true;
        }
      }
    }
    return false;
  }
}
