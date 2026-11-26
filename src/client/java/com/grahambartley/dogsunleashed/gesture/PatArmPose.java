package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

public final class PatArmPose {

  private PatArmPose() {}

  public static void applyTo(
      final ModelPart mainArm,
      final ModelPart head,
      final LivingEntity entity,
      final float tickDelta) {
    final float elapsed = elapsed(entity, tickDelta);
    if (elapsed == PlayerGesturePoses.NOT_PLAYING) {
      return;
    }
    mainArm.pitch = PatStroke.armPitch(mainArm.pitch, head.pitch, elapsed);
    mainArm.yaw = PatStroke.armYaw(mainArm.yaw, head.yaw, entity.getMainArm(), elapsed);
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
    matrices.multiply(
        RotationAxis.POSITIVE_X.rotationDegrees(-PatStroke.firstPersonTiltDegrees(elapsed)));
  }

  private static float elapsed(final LivingEntity entity, final float tickDelta) {
    return PlayerGesturePoses.SHARED.elapsedTicks(
        entity.getId(), Hand.MAIN_HAND, PlayerGesture.PAT, entity.getWorld().getTime(), tickDelta);
  }
}
