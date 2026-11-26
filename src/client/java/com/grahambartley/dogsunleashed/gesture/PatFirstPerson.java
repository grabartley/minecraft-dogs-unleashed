package com.grahambartley.dogsunleashed.gesture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PatFirstPerson {

  static final float MAX_REACH = 0.85f;
  static final float STROKE_REACH = 0.9f;
  private static final float PIXEL = 1.0f / 16.0f;

  private PatFirstPerson() {}

  public static void applyTo(
      final MatrixStack matrices, final LivingEntity player, final Arm arm, final float tickDelta) {
    if (arm != player.getMainArm()) {
      return;
    }
    PatBodyPose.frameOf(player, tickDelta)
        .ifPresent(
            frame -> {
              final Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
              PatTarget.crownOf(frame.dog(), tickDelta)
                  .ifPresent(
                      crown -> {
                        final Vector3f stroke = frame.rotation("right_arm");
                        placement(
                                arm,
                                toView(
                                    crown.subtract(camera.getPos()),
                                    camera.getYaw(),
                                    camera.getPitch()),
                                stroke.x,
                                stroke.y * frame.mirror(),
                                frame.moment().aim())
                            .applyTo(matrices);
                      });
            });
  }

  record Placement(Vector3f shoulder, Quaternionf turn, Vector3f reach) {

    void applyTo(final MatrixStack matrices) {
      matrices.translate(
          this.shoulder.x + this.reach.x,
          this.shoulder.y + this.reach.y,
          this.shoulder.z + this.reach.z);
      matrices.multiply(this.turn);
      matrices.translate(-this.shoulder.x, -this.shoulder.y, -this.shoulder.z);
    }

    Vector3f place(final Vector3f point) {
      return new Vector3f(point)
          .sub(this.shoulder)
          .rotate(this.turn)
          .add(this.shoulder)
          .add(this.reach);
    }
  }

  static Placement placement(
      final Arm arm,
      final Vector3f crownInView,
      final float strokePitch,
      final float strokeYaw,
      final float aim) {
    final Vector3f shoulder = restShoulder(arm);
    final Vector3f tip = restTip(arm);
    final float distance = crownInView.length();
    if (distance < 1.0e-4f || aim <= 0.0f) {
      return new Placement(shoulder, new Quaternionf(), new Vector3f());
    }
    final Vector3f toward = new Vector3f(crownInView).div(distance);
    final Vector3f target =
        new Vector3f(toward)
            .mul(Math.min(distance, MAX_REACH) - strokePitch * STROKE_REACH)
            .add(strokeYaw * STROKE_REACH, 0.0f, 0.0f);
    final Vector3f restArm = new Vector3f(tip).sub(shoulder);
    final Vector3f wantArm = new Vector3f(target).sub(shoulder);
    final Quaternionf turn =
        new Quaternionf().slerp(new Quaternionf().rotationTo(restArm, wantArm), aim);
    final Vector3f turnedTip = new Vector3f(restArm).rotate(turn).add(shoulder);
    final Vector3f reach = new Vector3f(target).sub(turnedTip).mul(aim);
    return new Placement(shoulder, turn, reach);
  }

  static Vector3f restShoulder(final Arm arm) {
    final float side = arm == Arm.RIGHT ? 1.0f : -1.0f;
    return chain(side).transformPosition(new Vector3f(-side * 5.0f * PIXEL, 2.0f * PIXEL, 0.0f));
  }

  static Vector3f restTip(final Arm arm) {
    final float side = arm == Arm.RIGHT ? 1.0f : -1.0f;
    return chain(side).transformPosition(new Vector3f(-side * 6.0f * PIXEL, 12.0f * PIXEL, 0.0f));
  }

  private static Matrix4f chain(final float side) {
    return new Matrix4f()
        .translate(side * 0.64000005f, -0.6f, -0.71999997f)
        .rotateY(side * 45.0f * MathHelper.RADIANS_PER_DEGREE)
        .translate(side * -1.0f, 3.6f, 3.5f)
        .rotateZ(side * 120.0f * MathHelper.RADIANS_PER_DEGREE)
        .rotateX(200.0f * MathHelper.RADIANS_PER_DEGREE)
        .rotateY(side * -135.0f * MathHelper.RADIANS_PER_DEGREE)
        .translate(side * 5.6f, 0.0f, 0.0f);
  }

  static Vector3f toView(final Vec3d offset, final float yawDegrees, final float pitchDegrees) {
    final Quaternionf rotation =
        new Quaternionf()
            .rotationYXZ(
                MathHelper.PI - yawDegrees * MathHelper.RADIANS_PER_DEGREE,
                -pitchDegrees * MathHelper.RADIANS_PER_DEGREE,
                0.0f)
            .conjugate();
    return rotation.transform(new Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
  }
}
