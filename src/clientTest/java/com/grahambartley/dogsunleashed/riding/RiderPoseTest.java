package com.grahambartley.dogsunleashed.riding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiderPoseTest {

  private static final float EPSILON = 1.0e-4f;

  private static RiderPose pose(final String json) {
    return RiderPose.parse(JsonParser.parseString(json).getAsJsonObject(), RiderPose.UPRIGHT);
  }

  @Test
  @DisplayName(
      "a pose reads its lean and limb angles, and keeps the fallback for anything left out")
  void parsesWithFallback() {
    final RiderPose pose = pose("{\"lean\": 20, \"right_arm\": [-40, -15, 0], \"left_leg\": [1]}");

    assertEquals(20.0f, pose.lean(), EPSILON);
    assertEquals(new Vector3f(-40, -15, 0), pose.rightArm());
    assertEquals(RiderPose.UPRIGHT.leftArm(), pose.leftArm());
    assertEquals(RiderPose.UPRIGHT.leftLeg(), pose.leftLeg());
  }

  @Test
  @DisplayName("blending moves every angle part of the way and never past either end")
  void blends() {
    final RiderPose rest = pose("{\"lean\": 10, \"right_arm\": [-40, 0, 0]}");
    final RiderPose gallop = pose("{\"lean\": 30, \"right_arm\": [-20, 0, 0]}");

    assertEquals(20.0f, rest.blend(gallop, 0.5f).lean(), EPSILON);
    assertEquals(-30.0f, rest.blend(gallop, 0.5f).rightArm().x, EPSILON);
    assertEquals(30.0f, rest.blend(gallop, 4.0f).lean(), EPSILON);
    assertEquals(10.0f, rest.blend(gallop, -1.0f).lean(), EPSILON);
  }

  @Test
  @DisplayName("applying a pose sets the limbs and lifts the head against the lean")
  void appliesToTheModel() {
    final BipedEntityModel<?> model =
        new BipedEntityModel<>(
            BipedEntityModel.getModelData(net.minecraft.client.model.Dilation.NONE, 0.0f)
                .getRoot()
                .createPart(64, 64));
    model.head.pitch = 0.1f;
    final RiderPose pose = pose("{\"lean\": 30, \"right_arm\": [-40, -15, 5]}");

    pose.applyTo(model, true);

    assertAngles(model.rightArm, -40, -15, 5);
    assertAngles(model.leftLeg, -81, -18, -4.5f);
    assertEquals(0.1f - 30 * MathHelper.RADIANS_PER_DEGREE, model.head.pitch, EPSILON);
    assertEquals(model.head.pitch, model.hat.pitch, EPSILON);
  }

  private static void assertAngles(
      final ModelPart part, final float x, final float y, final float z) {
    assertEquals(x * MathHelper.RADIANS_PER_DEGREE, part.pitch, EPSILON);
    assertEquals(y * MathHelper.RADIANS_PER_DEGREE, part.yaw, EPSILON);
    assertEquals(z * MathHelper.RADIANS_PER_DEGREE, part.roll, EPSILON);
  }

  @Test
  @DisplayName("busy hands keep vanilla's arms, so swings and aiming still show")
  void busyHandsKeepTheirArms() {
    final BipedEntityModel<?> model =
        new BipedEntityModel<>(
            BipedEntityModel.getModelData(net.minecraft.client.model.Dilation.NONE, 0.0f)
                .getRoot()
                .createPart(64, 64));
    model.rightArm.pitch = 1.2f;

    pose("{\"right_arm\": [-40, -15, 5]}").applyTo(model, false);

    assertEquals(1.2f, model.rightArm.pitch, EPSILON);
    assertAngles(model.rightLeg, -81, 18, 4.5f);
  }

  @Test
  @DisplayName("a bad number keeps the fallback instead of breaking the profile")
  void badNumbersFallBack() {
    final RiderPose pose = pose("{\"lean\": \"far\", \"right_arm\": [\"a\", 1, 2]}");

    assertEquals(RiderPose.UPRIGHT.lean(), pose.lean(), EPSILON);
    assertEquals(RiderPose.UPRIGHT.rightArm(), pose.rightArm());
  }

  @Test
  @DisplayName("a pose hands out copies, so nobody can bend the shared upright pose")
  void accessorsAreCopies() {
    RiderPose.UPRIGHT.rightArm().set(99, 99, 99);

    assertEquals(new Vector3f(-36, 0, 0), RiderPose.UPRIGHT.rightArm());
  }
}
