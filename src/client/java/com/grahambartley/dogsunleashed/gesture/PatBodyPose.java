package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.gesture.PlayerPoseClip.Channel;
import java.util.Optional;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class PatBodyPose {

  static final float BODY_LENGTH = 12.0f;
  static final float NECK_TO_SHOULDER = 2.0f;
  static final float NECK_TO_EYES = 4.0f;
  static final float STANDING_HEIGHT = 24.0f;
  static final float BLOCKS_PER_PIXEL = 0.9375f / 16.0f;
  static final float STANDING_ARM_SWAY = 0.5f;

  record Frame(
      PlayerPoseClip clip,
      PatTimeline.Moment moment,
      PatTarget.Crown crown,
      Arm mainArm,
      UnleashedDogEntity dog) {

    Vector3f rotation(final String bone) {
      return this.sample(bone, Channel.ROTATION).mul(MathHelper.RADIANS_PER_DEGREE);
    }

    float drop() {
      return -this.sample("hips", Channel.POSITION).y;
    }

    float holdDrop() {
      return -this.clip.sample("hips", Channel.POSITION, PatTimeline.KNEEL_END).y;
    }

    float holdLean() {
      return this.clip.sample("torso", Channel.ROTATION, PatTimeline.KNEEL_END).x
          * MathHelper.RADIANS_PER_DEGREE;
    }

    float mirror() {
      return this.mainArm == Arm.RIGHT ? 1.0f : -1.0f;
    }

    private Vector3f sample(final String bone, final Channel channel) {
      final Vector3f to = this.clip.sample(bone, channel, this.moment.clipTime());
      if (this.moment.fade() >= 1.0f) {
        return to;
      }
      return this.clip
          .sample(bone, channel, this.moment.fromClipTime())
          .lerp(to, this.moment.fade());
    }
  }

  private PatBodyPose() {}

  public static void applyTo(
      final PlayerEntityModel<?> model, final LivingEntity player, final float tickDelta) {
    frameOf(player, tickDelta).ifPresent(frame -> apply(model, frame));
  }

  public static float eyeDrop(final Entity entity, final float tickDelta) {
    if (!(entity instanceof LivingEntity player)) {
      return 0.0f;
    }
    return frameOf(player, tickDelta)
        .map(
            frame ->
                PatReach.kneelWeight(frame.crown().up())
                    * frame.drop()
                    * frame.moment().body()
                    * BLOCKS_PER_PIXEL)
        .orElse(0.0f);
  }

  static Optional<Frame> frameOf(final LivingEntity player, final float tickDelta) {
    if (player.hasVehicle()) {
      return Optional.empty();
    }
    final PlayerPoseClip clip = PlayerPoseClips.pat();
    if (clip.length() <= 0.0f) {
      return Optional.empty();
    }
    return PatSessions.SHARED
        .of(player.getId(), player.getWorld().getTime(), tickDelta, clip.length())
        .flatMap(
            active -> {
              final Entity target = player.getWorld().getEntityById(active.targetEntityId());
              if (!(target instanceof UnleashedDogEntity dog)) {
                return Optional.empty();
              }
              return PatTarget.of(player, dog, player.getMainArm(), tickDelta)
                  .map(crown -> new Frame(clip, active.moment(), crown, player.getMainArm(), dog));
            });
  }

  static void apply(final PlayerEntityModel<?> model, final Frame frame) {
    final float body = frame.moment().body();
    final float aim = frame.moment().aim();
    final float mirror = frame.mirror();
    final float kneel = PatReach.kneelWeight(frame.crown().up());
    final PatReach.Pose reach =
        PatReach.toward(
            frame.crown().up(),
            frame.crown().forward(),
            frame.crown().side(),
            frame.holdDrop(),
            frame.holdLean());

    final Vector3f torso = frame.rotation("torso");
    final float lean = torso.x + aim * Math.max(0.0f, reach.lean() - frame.holdLean());
    final float hipY = PatReach.HIP_HEIGHT + kneel * frame.drop();
    final float neckY = hipY - BODY_LENGTH * MathHelper.cos(lean);
    final float neckZ = -BODY_LENGTH * MathHelper.sin(lean);
    final float shoulderY = neckY + NECK_TO_SHOULDER * MathHelper.cos(lean);
    final float shoulderZ = neckZ + NECK_TO_SHOULDER * MathHelper.sin(lean);

    place(model.body, neckY, neckZ, body);
    model.body.pitch = MathHelper.lerp(body, model.body.pitch, lean);
    model.body.roll = MathHelper.lerp(body, model.body.roll, torso.z * mirror);

    place(model.head, neckY, neckZ, body);
    final Vector3f nod = frame.rotation("head");
    final float eyeUp = STANDING_HEIGHT - neckY + NECK_TO_EYES;
    final float crownUp = frame.crown().up() * PatReach.PIXELS_PER_BLOCK;
    final float crownForward = Math.max(1.0f, frame.crown().forward() * PatReach.PIXELS_PER_BLOCK);
    final float crownRight = frame.crown().side() * PatReach.PIXELS_PER_BLOCK * mirror;
    model.head.pitch =
        MathHelper.lerp(
            aim, model.head.pitch, (float) MathHelper.atan2(eyeUp - crownUp, crownForward) + nod.x);
    model.head.yaw =
        MathHelper.lerp(
            aim,
            model.head.yaw,
            (float) MathHelper.atan2(crownRight, crownForward) + nod.y * mirror);
    model.head.roll = MathHelper.lerp(body, model.head.roll, nod.z * mirror);

    final boolean right = frame.mainArm() == Arm.RIGHT;
    final ModelPart patting = right ? model.rightArm : model.leftArm;
    final ModelPart resting = right ? model.leftArm : model.rightArm;
    place(patting, shoulderY, shoulderZ, body);
    place(resting, shoulderY, shoulderZ, body);
    final Vector3f stroke = frame.rotation("right_arm");
    patting.pitch = MathHelper.lerp(aim, patting.pitch, reach.armPitch() + stroke.x);
    patting.yaw =
        MathHelper.lerp(aim, patting.yaw, -mirror * reach.armInward() + stroke.y * mirror);
    patting.roll = MathHelper.lerp(aim, patting.roll, stroke.z * mirror);
    final Vector3f rest = frame.rotation("left_arm");
    resting.pitch =
        MathHelper.lerp(
            body, resting.pitch, kneel * rest.x + (1.0f - kneel) * STANDING_ARM_SWAY * lean);
    resting.yaw = MathHelper.lerp(body, resting.yaw, kneel * rest.y * mirror);
    resting.roll = MathHelper.lerp(body, resting.roll, kneel * rest.z * mirror);

    final ModelPart kneeling = right ? model.rightLeg : model.leftLeg;
    final ModelPart planted = right ? model.leftLeg : model.rightLeg;
    place(kneeling, hipY, 0.0f, body);
    place(planted, hipY, 0.0f, body);
    final Vector3f back = frame.rotation("right_leg");
    final Vector3f front = frame.rotation("left_leg");
    kneeling.pitch = MathHelper.lerp(body, kneeling.pitch, kneel * back.x);
    kneeling.yaw = MathHelper.lerp(body, kneeling.yaw, kneel * back.y * mirror);
    planted.pitch = MathHelper.lerp(body, planted.pitch, kneel * front.x);
    planted.yaw = MathHelper.lerp(body, planted.yaw, kneel * front.y * mirror);

    model.jacket.copyTransform(model.body);
    model.hat.copyTransform(model.head);
    model.rightSleeve.copyTransform(model.rightArm);
    model.leftSleeve.copyTransform(model.leftArm);
    model.rightPants.copyTransform(model.rightLeg);
    model.leftPants.copyTransform(model.leftLeg);
  }

  private static void place(
      final ModelPart part, final float pivotY, final float pivotZ, final float weight) {
    part.pivotY = MathHelper.lerp(weight, part.pivotY, pivotY);
    part.pivotZ = MathHelper.lerp(weight, part.pivotZ, pivotZ);
  }
}
