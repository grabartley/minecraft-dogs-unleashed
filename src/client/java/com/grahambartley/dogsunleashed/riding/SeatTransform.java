package com.grahambartley.dogsunleashed.riding;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record SeatTransform(Vector3f offset, Quaternionf rotation) {

  public SeatTransform {
    offset = new Vector3f(offset);
    rotation = new Quaternionf(rotation);
  }

  public static SeatTransform between(
      final Matrix4f entityOrigin, final Matrix4f seat, final float modelYawOffsetDegrees) {
    final Matrix4f relative = new Matrix4f(entityOrigin).invert().mul(seat);
    final Quaternionf rotation =
        relative
            .getUnnormalizedRotation(new Quaternionf())
            .rotateY((float) Math.toRadians(-modelYawOffsetDegrees));
    return new SeatTransform(relative.getTranslation(new Vector3f()), rotation);
  }

  public Vector3f riderOrigin(final Vec3d riderAttachment, final float leanDegrees) {
    return this.riderRotation(leanDegrees)
        .transform(
            new Vector3f(
                (float) -riderAttachment.x, (float) -riderAttachment.y, (float) -riderAttachment.z))
        .add(this.offset);
  }

  public Quaternionf riderRotation(final float leanDegrees) {
    return new Quaternionf(this.rotation).rotateX((float) Math.toRadians(-leanDegrees));
  }

  public void moveToRider(
      final MatrixStack matrices, final Vec3d riderAttachment, final float leanDegrees) {
    matrices.translate(this.offset.x, this.offset.y, this.offset.z);
    matrices.multiply(this.riderRotation(leanDegrees));
    matrices.translate(-riderAttachment.x, -riderAttachment.y, -riderAttachment.z);
  }

  @Override
  public Vector3f offset() {
    return new Vector3f(this.offset);
  }

  @Override
  public Quaternionf rotation() {
    return new Quaternionf(this.rotation);
  }
}
