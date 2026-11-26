package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import java.util.Optional;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public final class PatArmPose {

  static final float BODY_LENGTH = 12.0f;
  static final float NECK_TO_SHOULDER = 2.0f;
  static final float BACK_KNEE = (float) Math.toRadians(25.0);
  static final float FRONT_KNEE = (float) Math.toRadians(-75.0);
  static final float FRONT_SPLAY = (float) Math.toRadians(30.0);
  static final float OFF_ARM_SWAY = 0.5f;

  private PatArmPose() {}

  public static void applyTo(
      final PlayerEntityModel<?> model, final LivingEntity player, final float tickDelta) {
    final float elapsed = elapsed(player, tickDelta);
    if (elapsed == PlayerGesturePoses.NOT_PLAYING || player.hasVehicle()) {
      return;
    }
    poseToward(player, tickDelta)
        .ifPresent(pose -> apply(model, player.getMainArm(), pose, elapsed));
  }

  public static float eyeDrop(final Entity entity, final float tickDelta) {
    if (!(entity instanceof LivingEntity player) || player.hasVehicle()) {
      return 0.0f;
    }
    final float elapsed = elapsed(player, tickDelta);
    if (elapsed == PlayerGesturePoses.NOT_PLAYING) {
      return 0.0f;
    }
    return poseToward(player, tickDelta)
        .map(pose -> pose.eyeDropBlocks() * PatStroke.reach(elapsed))
        .orElse(0.0f);
  }

  public static void applyFirstPerson(
      final MatrixStack matrices, final LivingEntity player, final Arm arm, final float tickDelta) {
    if (arm != player.getMainArm()) {
      return;
    }
    final float elapsed = elapsed(player, tickDelta);
    if (elapsed == PlayerGesturePoses.NOT_PLAYING) {
      return;
    }
    final Vector3f shift = PatStroke.firstPersonShift(arm, elapsed);
    matrices.translate(shift.x, shift.y, shift.z);
  }

  static void apply(
      final PlayerEntityModel<?> model,
      final Arm mainArm,
      final PatReach.Pose pose,
      final float elapsed) {
    final float blend = PatStroke.reach(elapsed);
    final float lean = pose.lean() * blend;
    final float hipY = PatReach.HIP_HEIGHT + pose.drop() * blend;
    final float neckY = hipY - BODY_LENGTH * MathHelper.cos(lean);
    final float neckZ = -BODY_LENGTH * MathHelper.sin(lean);
    final float shoulderY = neckY + NECK_TO_SHOULDER * MathHelper.cos(lean);
    final float shoulderZ = neckZ + NECK_TO_SHOULDER * MathHelper.sin(lean);
    final float kneel = pose.drop() > 0.0f ? blend : 0.0f;
    final float side = PatStroke.side(mainArm);

    place(model.body, neckY, neckZ, blend);
    model.body.pitch = MathHelper.lerp(blend, model.body.pitch, pose.lean());
    place(model.head, neckY, neckZ, blend);

    final ModelPart patting = mainArm == Arm.RIGHT ? model.rightArm : model.leftArm;
    final ModelPart resting = mainArm == Arm.RIGHT ? model.leftArm : model.rightArm;
    place(patting, shoulderY, shoulderZ, blend);
    place(resting, shoulderY, shoulderZ, blend);
    patting.pitch =
        MathHelper.lerp(blend, patting.pitch, pose.armPitch() - PatStroke.liftRadians(elapsed));
    patting.yaw = MathHelper.lerp(blend, patting.yaw, -side * pose.armInward());
    patting.roll = MathHelper.lerp(blend, patting.roll, 0.0f);
    resting.pitch = MathHelper.lerp(blend, resting.pitch, OFF_ARM_SWAY * pose.lean());

    final ModelPart kneeling = mainArm == Arm.RIGHT ? model.rightLeg : model.leftLeg;
    final ModelPart planted = mainArm == Arm.RIGHT ? model.leftLeg : model.rightLeg;
    place(kneeling, hipY, 0.0f, blend);
    place(planted, hipY, 0.0f, blend);
    kneeling.pitch = MathHelper.lerp(blend, kneeling.pitch, kneel * BACK_KNEE);
    kneeling.yaw = MathHelper.lerp(blend, kneeling.yaw, 0.0f);
    planted.pitch = MathHelper.lerp(blend, planted.pitch, kneel * FRONT_KNEE);
    planted.yaw = MathHelper.lerp(blend, planted.yaw, -side * kneel * FRONT_SPLAY);

    model.jacket.copyTransform(model.body);
    model.hat.copyTransform(model.head);
    model.rightSleeve.copyTransform(model.rightArm);
    model.leftSleeve.copyTransform(model.leftArm);
    model.rightPants.copyTransform(model.rightLeg);
    model.leftPants.copyTransform(model.leftLeg);
  }

  private static void place(
      final ModelPart part, final float pivotY, final float pivotZ, final float blend) {
    part.pivotY = MathHelper.lerp(blend, part.pivotY, pivotY);
    part.pivotZ = MathHelper.lerp(blend, part.pivotZ, pivotZ);
  }

  private static Optional<PatReach.Pose> poseToward(
      final LivingEntity player, final float tickDelta) {
    return PlayerGesturePoses.SHARED
        .targetOf(player.getId(), PlayerGesture.PAT, player.getWorld().getTime())
        .stream()
        .mapToObj(id -> player.getWorld().getEntityById(id))
        .filter(UnleashedDogEntity.class::isInstance)
        .map(UnleashedDogEntity.class::cast)
        .findFirst()
        .flatMap(dog -> PatTarget.of(player, dog, player.getMainArm(), tickDelta))
        .map(crown -> PatReach.toward(crown.up(), crown.forward(), crown.side()));
  }

  private static float elapsed(final LivingEntity entity, final float tickDelta) {
    return PlayerGesturePoses.SHARED.elapsedTicks(
        entity.getId(), Hand.MAIN_HAND, PlayerGesture.PAT, entity.getWorld().getTime(), tickDelta);
  }
}
