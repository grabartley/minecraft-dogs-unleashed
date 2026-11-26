package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Part;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Pose;
import com.grahambartley.dogsunleashed.render.bend.BendablePart;
import java.util.Optional;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PatBodyPose {

  static final float EYE_ABOVE_NECK = 4.0f;

  static final float HEAD_TURN_LIMIT = 75.0f * MathHelper.RADIANS_PER_DEGREE;
  static final float BLOCKS_PER_PIXEL = 0.9375f / 16.0f;

  public record Frame(
      PlayerPoseClips.Pat clips,
      PatTimeline.Moment moment,
      PatTarget.Aim aim,
      PatTarget.Frame space) {

    Bones bonesAt(final float seconds) {
      return PatBones.blend(
          PatBones.sample(this.clips.stand(), seconds),
          PatBones.sample(this.clips.kneel(), seconds),
          this.aim.kneel());
    }

    Bones bones() {
      final Bones to = this.bonesAt(this.moment.clipTime());
      return this.moment.fade() >= 1.0f
          ? to
          : PatBones.blend(this.bonesAt(this.moment.fromClipTime()), to, this.moment.fade());
    }
  }

  public record Solved(Pose pose, Vector3f palmTarget) {}

  private PatBodyPose() {}

  public static void applyTo(
      final PlayerEntityModel<?> model, final LivingEntity player, final float tickDelta) {
    frameOf(player, tickDelta).ifPresent(frame -> apply(model, frame));
  }

  public static float cameraLowering(
      final Entity entity, final float tickDelta, final float currentEyeHeight) {
    if (!(entity instanceof LivingEntity player)) {
      return 0.0f;
    }
    return frameOf(player, tickDelta)
        .map(
            frame ->
                frame.moment().body()
                    * (currentEyeHeight
                        - player.getEyeHeight(EntityPose.STANDING)
                        + solve(frame).pose().head().pivot().y * BLOCKS_PER_PIXEL))
        .orElse(0.0f);
  }

  public static float bodyWeight(final LivingEntity player, final float tickDelta) {
    return frameOf(player, tickDelta).map(frame -> frame.moment().body()).orElse(0.0f);
  }

  static Optional<Frame> frameOf(final LivingEntity player, final float tickDelta) {
    if (player.hasVehicle()) {
      return Optional.empty();
    }
    final PlayerPoseClips.Pat clips = PlayerPoseClips.pat();
    if (clips.length() <= 0.0f) {
      return Optional.empty();
    }
    final long now = player.getWorld().getTime();
    return PatSessions.SHARED
        .of(player.getId(), now, tickDelta, clips.length())
        .flatMap(
            active -> {
              final PatTarget.Frame space =
                  PatTarget.frameOf(player, player.getMainArm(), tickDelta);
              final Entity target = player.getWorld().getEntityById(active.targetEntityId());
              final Optional<PatTarget.Aim> aim =
                  target instanceof UnleashedDogEntity dog && dog.isAlive()
                      ? PatTarget.of(space, dog, tickDelta)
                      : Optional.empty();
              if (aim.isPresent()) {
                PatSessions.SHARED.remember(player.getId(), aim.get());
                return aim.map(found -> new Frame(clips, active.moment(), found, space));
              }
              PatSessions.SHARED.lose(player.getId(), now);
              return active.lastAim().map(last -> new Frame(clips, active.moment(), last, space));
            });
  }

  static Solved solve(final Frame frame) {
    final float aim = frame.moment().aim();
    final Bones bones = frame.bones();
    final Bones hold = frame.bonesAt(PatTimeline.KNEEL_END);
    final Vector3f stroke =
        PatSkeleton.rightPalm(PatSkeleton.of(bones).rightArm())
            .sub(PatSkeleton.rightPalm(PatSkeleton.of(hold).rightArm()))
            .mul(frame.aim().strokeScale());
    final Vector3f palmTarget = new Vector3f(frame.aim().neck()).add(stroke);
    final Pose pose =
        PatSkeleton.of(PatBones.leanedIn(bones, aim * PatReach.extraLean(bones, palmTarget)));
    final Part reached =
        ArmReach.toward(pose.rightArm(), palmTarget, PatSkeleton.rightElbow(pose.rightArm()));
    final Part arm =
        new Part(
            pose.rightArm().pivot(),
            new Quaternionf(pose.rightArm().rotation()).slerp(reached.rotation(), aim),
            MathHelper.lerp(aim, pose.rightArm().bend(), reached.bend()));
    final Quaternionf look =
        lookAt(pose.head().pivot(), pose.body().rotation(), frame.aim().crown())
            .mul(new Quaternionf(hold.head()).conjugate().mul(bones.head()));
    final Part head =
        new Part(
            pose.head().pivot(), new Quaternionf(pose.head().rotation()).slerp(look, aim), 0.0f);
    return new Solved(pose.withRightArm(arm).withHead(head), palmTarget);
  }

  static Quaternionf lookAt(final Vector3f neck, final Quaternionf torso, final Vector3f crown) {
    final Vector3f eye = torso.transform(new Vector3f(0.0f, -EYE_ABOVE_NECK, 0.0f)).add(neck);
    final Vector3f toward = new Vector3f(crown).sub(eye);
    final Vector3f facing = torso.transform(new Vector3f(0.0f, 0.0f, -1.0f));
    final float torsoYaw = (float) Math.atan2(-facing.x, -facing.z);
    final float yaw =
        torsoYaw
            + MathHelper.clamp(
                MathHelper.wrapDegrees(
                        ((float) Math.atan2(-toward.x, -toward.z) - torsoYaw)
                            * MathHelper.DEGREES_PER_RADIAN)
                    * MathHelper.RADIANS_PER_DEGREE,
                -HEAD_TURN_LIMIT,
                HEAD_TURN_LIMIT);
    final float pitch =
        (float) Math.atan2(toward.y, Math.max(1.0e-3, Math.hypot(toward.x, toward.z)));
    return new Quaternionf().rotationZYX(0.0f, yaw, pitch);
  }

  static void apply(final PlayerEntityModel<?> model, final Frame frame) {
    final Pose canonical = solve(frame).pose();
    final Pose pose = frame.space().mirror() < 0.0f ? canonical.mirrored() : canonical;
    final float body = frame.moment().body();
    place(model.body, pose.body(), body, 0.0f);
    place(model.head, pose.head(), body, 0.0f);
    place(model.rightArm, pose.rightArm(), body, PatSkeleton.ELBOW);
    place(model.leftArm, pose.leftArm(), body, PatSkeleton.ELBOW);
    place(model.rightLeg, pose.rightLeg(), body, PatSkeleton.KNEE);
    place(model.leftLeg, pose.leftLeg(), body, PatSkeleton.KNEE);
    model.jacket.copyTransform(model.body);
    model.hat.copyTransform(model.head);
    model.rightSleeve.copyTransform(model.rightArm);
    model.leftSleeve.copyTransform(model.leftArm);
    model.rightPants.copyTransform(model.rightLeg);
    model.leftPants.copyTransform(model.leftLeg);
  }

  private static void place(
      final ModelPart part, final Part pose, final float weight, final float joint) {
    final Vector3f angles = pose.angles();
    part.pivotX = MathHelper.lerp(weight, part.pivotX, pose.pivot().x);
    part.pivotY = MathHelper.lerp(weight, part.pivotY, pose.pivot().y);
    part.pivotZ = MathHelper.lerp(weight, part.pivotZ, pose.pivot().z);
    part.pitch = lerpAngle(weight, part.pitch, angles.x);
    part.yaw = lerpAngle(weight, part.yaw, angles.y);
    part.roll = lerpAngle(weight, part.roll, angles.z);
    if (joint > 0.0f) {
      BendablePart.of(part).dogsUnleashed$setBend(joint, weight * pose.bend());
    }
  }

  private static float lerpAngle(final float weight, final float from, final float to) {
    return from
        + MathHelper.wrapDegrees((to - from) * MathHelper.DEGREES_PER_RADIAN)
            * MathHelper.RADIANS_PER_DEGREE
            * weight;
  }
}
