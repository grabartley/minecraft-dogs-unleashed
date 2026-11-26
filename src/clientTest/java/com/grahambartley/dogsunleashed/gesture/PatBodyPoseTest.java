package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PatBodyPoseTest {

  private static final float EPSILON = 1.0e-4f;

  private static PlayerPoseClip clip() throws IOException {
    try (Reader reader =
        Files.newBufferedReader(
            Path.of("src/client/resources/assets/dogs-unleashed/player_poses/pat.json"),
            StandardCharsets.UTF_8)) {
      return PlayerPoseClip.parse(JsonParser.parseReader(reader).getAsJsonObject(), "pat");
    }
  }

  private static PlayerEntityModel<LivingEntity> model() {
    return new PlayerEntityModel<>(
        TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
            .createModel(),
        false);
  }

  private static PatBodyPose.Frame frame(
      final float up, final Arm arm, final float aim, final float body) throws IOException {
    return new PatBodyPose.Frame(
        clip(),
        new PatTimeline.Moment(1.0f, 1.0f, 1.0f, aim, body),
        new PatTarget.Crown(up, 0.6f, 0.0f),
        arm,
        null);
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("for a low dog the player kneels on the patting side and plants the other foot")
  void aLowDogGetsAKneel(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(0.5f, arm, 1.0f, 1.0f));
    final boolean right = arm == Arm.RIGHT;
    final var kneeling = right ? model.rightLeg : model.leftLeg;
    final var planted = right ? model.leftLeg : model.rightLeg;
    assertTrue(kneeling.pivotY > 17.0f, "the hips drop towards the ground");
    assertTrue(kneeling.pitch > 0.0f, "the kneeling leg goes back to put the knee down");
    assertTrue(planted.pitch < 0.0f, "the other leg steps forward");
    assertTrue(
        Math.signum(planted.yaw) == (right ? -1.0f : 1.0f),
        "the forward leg splays out the way vanilla's sitting pose does");
    assertTrue(model.head.pitch > 0.0f, "the player looks down at the dog");
  }

  @ParameterizedTest(name = "crown {0} blocks up")
  @CsvSource({"1.05", "1.6"})
  @DisplayName("for a tall dog the player stays standing")
  void aTallDogGetsAStandingPat(final float up) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(up, Arm.RIGHT, 1.0f, 1.0f));
    assertEquals(12.0f, model.rightLeg.pivotY, 0.5f);
    assertEquals(0.0f, model.rightLeg.pitch, EPSILON);
    assertEquals(0.0f, model.leftLeg.pitch, EPSILON);
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("the patting hand reaches out and the skin layers follow every part")
  void theHandReachesAndLayersFollow(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(0.62f, arm, 1.0f, 1.0f));
    final var patting = arm == Arm.RIGHT ? model.rightArm : model.leftArm;
    assertTrue(patting.pitch < -MathHelper.PI / 6.0f, "the arm reaches forward");
    assertEquals(model.rightArm.pitch, model.rightSleeve.pitch, EPSILON);
    assertEquals(model.leftLeg.yaw, model.leftPants.yaw, EPSILON);
    assertEquals(model.body.pivotY, model.jacket.pivotY, EPSILON);
    assertEquals(model.head.pitch, model.hat.pitch, EPSILON);
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("with no weight the vanilla pose is left alone")
  void noWeightLeavesVanillaAlone(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    model.rightArm.pitch = 0.3f;
    model.leftLeg.pitch = -0.2f;
    model.head.pitch = 0.1f;
    PatBodyPose.apply(model, frame(0.5f, arm, 0.0f, 0.0f));
    assertEquals(0.3f, model.rightArm.pitch, EPSILON);
    assertEquals(-0.2f, model.leftLeg.pitch, EPSILON);
    assertEquals(0.1f, model.head.pitch, EPSILON);
    assertEquals(12.0f, model.leftLeg.pivotY, EPSILON);
  }
}
