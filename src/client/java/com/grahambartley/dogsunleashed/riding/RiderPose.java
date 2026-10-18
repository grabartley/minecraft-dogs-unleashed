package com.grahambartley.dogsunleashed.riding;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

public record RiderPose(
    float lean, Vector3f rightArm, Vector3f leftArm, Vector3f rightLeg, Vector3f leftLeg) {

  public static final RiderPose UPRIGHT =
      new RiderPose(
          0.0f,
          new Vector3f(-36.0f, 0.0f, 0.0f),
          new Vector3f(-36.0f, 0.0f, 0.0f),
          new Vector3f(-81.0f, 18.0f, 4.5f),
          new Vector3f(-81.0f, -18.0f, -4.5f));

  public RiderPose {
    rightArm = new Vector3f(rightArm);
    leftArm = new Vector3f(leftArm);
    rightLeg = new Vector3f(rightLeg);
    leftLeg = new Vector3f(leftLeg);
  }

  public static RiderPose parse(final JsonObject json, final RiderPose fallback) {
    return new RiderPose(
        json.has("lean") ? json.get("lean").getAsFloat() : fallback.lean,
        angles(json.get("right_arm"), fallback.rightArm),
        angles(json.get("left_arm"), fallback.leftArm),
        angles(json.get("right_leg"), fallback.rightLeg),
        angles(json.get("left_leg"), fallback.leftLeg));
  }

  private static Vector3f angles(final JsonElement element, final Vector3f fallback) {
    if (element instanceof JsonArray xyz && xyz.size() == 3) {
      return new Vector3f(
          xyz.get(0).getAsFloat(), xyz.get(1).getAsFloat(), xyz.get(2).getAsFloat());
    }
    return fallback;
  }

  public RiderPose blend(final RiderPose toward, final float amount) {
    final float t = MathHelper.clamp(amount, 0.0f, 1.0f);
    return new RiderPose(
        MathHelper.lerp(t, this.lean, toward.lean),
        new Vector3f(this.rightArm).lerp(toward.rightArm, t),
        new Vector3f(this.leftArm).lerp(toward.leftArm, t),
        new Vector3f(this.rightLeg).lerp(toward.rightLeg, t),
        new Vector3f(this.leftLeg).lerp(toward.leftLeg, t));
  }

  public void applyTo(final BipedEntityModel<?> model) {
    set(model.rightArm, this.rightArm);
    set(model.leftArm, this.leftArm);
    set(model.rightLeg, this.rightLeg);
    set(model.leftLeg, this.leftLeg);
    model.head.pitch -= this.lean * MathHelper.RADIANS_PER_DEGREE;
    model.hat.copyTransform(model.head);
  }

  private static void set(final ModelPart part, final Vector3f degrees) {
    part.setAngles(
        degrees.x * MathHelper.RADIANS_PER_DEGREE,
        degrees.y * MathHelper.RADIANS_PER_DEGREE,
        degrees.z * MathHelper.RADIANS_PER_DEGREE);
  }
}
