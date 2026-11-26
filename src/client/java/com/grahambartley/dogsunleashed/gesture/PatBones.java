package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import com.grahambartley.dogsunleashed.gesture.PlayerPoseClip.Channel;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PatBones {

  private PatBones() {}

  public static Bones sample(final PlayerPoseClip clip, final float seconds) {
    final Vector3f hips = clip.sample("hips", Channel.POSITION, seconds);
    return new Bones(
        new Vector3f(hips.x, -hips.y, hips.z),
        rotation(clip, "hips", seconds),
        rotation(clip, "torso", seconds),
        rotation(clip, "head", seconds),
        rotation(clip, "right_arm", seconds),
        bend(clip, "right_forearm", seconds),
        rotation(clip, "left_arm", seconds),
        bend(clip, "left_forearm", seconds),
        rotation(clip, "right_leg", seconds),
        bend(clip, "right_shin", seconds),
        rotation(clip, "left_leg", seconds),
        bend(clip, "left_shin", seconds));
  }

  public static Bones blend(final Bones from, final Bones to, final float t) {
    if (t <= 0.0f) {
      return from;
    }
    if (t >= 1.0f) {
      return to;
    }
    return new Bones(
        new Vector3f(from.hipsOffset()).lerp(to.hipsOffset(), t),
        new Quaternionf(from.hips()).slerp(to.hips(), t),
        new Quaternionf(from.torso()).slerp(to.torso(), t),
        new Quaternionf(from.head()).slerp(to.head(), t),
        new Quaternionf(from.rightArm()).slerp(to.rightArm(), t),
        MathHelper.lerp(t, from.rightElbow(), to.rightElbow()),
        new Quaternionf(from.leftArm()).slerp(to.leftArm(), t),
        MathHelper.lerp(t, from.leftElbow(), to.leftElbow()),
        new Quaternionf(from.rightLeg()).slerp(to.rightLeg(), t),
        MathHelper.lerp(t, from.rightKnee(), to.rightKnee()),
        new Quaternionf(from.leftLeg()).slerp(to.leftLeg(), t),
        MathHelper.lerp(t, from.leftKnee(), to.leftKnee()));
  }

  public static Bones leanedIn(final Bones bones, final float extraLean) {
    return new Bones(
        bones.hipsOffset(),
        bones.hips(),
        new Quaternionf(bones.torso()).rotateX(extraLean),
        bones.head(),
        bones.rightArm(),
        bones.rightElbow(),
        bones.leftArm(),
        bones.leftElbow(),
        bones.rightLeg(),
        bones.rightKnee(),
        bones.leftLeg(),
        bones.leftKnee());
  }

  private static Quaternionf rotation(
      final PlayerPoseClip clip, final String bone, final float seconds) {
    final Vector3f degrees = clip.sample(bone, Channel.ROTATION, seconds);
    return new Quaternionf()
        .rotationZYX(
            degrees.z * MathHelper.RADIANS_PER_DEGREE,
            degrees.y * MathHelper.RADIANS_PER_DEGREE,
            degrees.x * MathHelper.RADIANS_PER_DEGREE);
  }

  private static float bend(final PlayerPoseClip clip, final String bone, final float seconds) {
    return clip.sample(bone, Channel.ROTATION, seconds).x * MathHelper.RADIANS_PER_DEGREE;
  }
}
