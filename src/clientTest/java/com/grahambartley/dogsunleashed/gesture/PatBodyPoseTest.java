package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.render.bend.BendablePart;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class PatBodyPoseTest {

  private static final float EPSILON = 1.0e-4f;
  private static final Vector3f CROWN = new Vector3f(-3.0f, 13.0f, -12.0f);

  private static PlayerPoseClips.Pat clips() throws IOException {
    try (Reader reader =
        Files.newBufferedReader(
            Path.of("src/client/resources/assets/dogs-unleashed/player_poses/pat.json"),
            StandardCharsets.UTF_8)) {
      final JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
      return new PlayerPoseClips.Pat(
          PlayerPoseClip.parse(file, PlayerPoseClips.KNEEL_CLIP),
          PlayerPoseClip.parse(file, PlayerPoseClips.STAND_CLIP));
    }
  }

  private static PlayerEntityModel<LivingEntity> model() {
    return new PlayerEntityModel<>(
        TexturedModelData.of(PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
            .createModel(),
        false);
  }

  private static PatBodyPose.Frame frame(
      final float kneel, final Arm arm, final float aim, final float body, final Vector3f neck)
      throws IOException {
    return new PatBodyPose.Frame(
        clips(),
        new PatTimeline.Moment(1.0f, 1.0f, 1.0f, aim, body),
        new PatTarget.Aim(CROWN, neck, kneel, 1.0f),
        new PatTarget.Frame(Vec3d.ZERO, 0.0f, arm == Arm.RIGHT ? 1.0f : -1.0f));
  }

  private static float bendOf(final ModelPart part) {
    return BendablePart.of(part).dogsUnleashed$bendAngle();
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("for a small dog the player goes down on the patting side's knee")
  void aSmallDogGetsAKneel(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(1.0f, arm, 1.0f, 1.0f, new Vector3f(-4, 14, -8)));
    final boolean right = arm == Arm.RIGHT;
    final ModelPart kneeling = right ? model.rightLeg : model.leftLeg;
    final ModelPart planted = right ? model.leftLeg : model.rightLeg;
    assertTrue(kneeling.pivotY > 14.0f, "the hips drop towards the ground");
    assertTrue(bendOf(kneeling) > 1.2f, "the back shin folds flat behind the knee");
    assertTrue(planted.pitch < -0.8f, "the front thigh comes up");
    assertTrue(bendOf(planted) > 0.8f, "the front shin comes down to the foot");
    assertTrue(model.head.pitch > 0.0f, "the player looks down at the dog");
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("for a big dog the player stays on their feet")
  void aBigDogGetsAStandingPat(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(0.0f, arm, 1.0f, 1.0f, new Vector3f(-5, 4, -9)));
    assertEquals(12.0f, model.rightLeg.pivotY, 1.0f);
    assertTrue(Math.abs(bendOf(model.rightLeg)) < 0.3f);
    assertTrue(Math.abs(bendOf(model.leftLeg)) < 0.3f);
  }

  @ParameterizedTest(name = "kneel {0}, offset {1},{2},{3}")
  @CsvSource({"1, 0.5, 0.5, -0.5", "1, -1, 1, 0", "0, 0.5, -0.5, -1", "0.5, 0, 0, 0"})
  @DisplayName("the patting palm lands on the dog's neck")
  void thePalmLandsOnTheNeck(final float kneel, final float dx, final float dy, final float dz)
      throws IOException {
    final PatBodyPose.Frame probe = frame(kneel, Arm.RIGHT, 0.0f, 1.0f, new Vector3f());
    final Vector3f held =
        PatSkeleton.rightPalm(PatSkeleton.of(probe.bonesAt(PatTimeline.KNEEL_END)).rightArm());
    final Vector3f neck = held.add(dx, dy, dz);
    final PatBodyPose.Solved solved = PatBodyPose.solve(frame(kneel, Arm.RIGHT, 1.0f, 1.0f, neck));
    final Vector3f palm = PatSkeleton.rightPalm(solved.pose().rightArm());
    assertEquals(0.0f, palm.distance(solved.palmTarget()), 0.3f);
  }

  @ParameterizedTest(name = "{0} handed")
  @EnumSource(Arm.class)
  @DisplayName("the skin's outer layers and bends follow every part")
  void theLayersFollow(final Arm arm) throws IOException {
    final PlayerEntityModel<LivingEntity> model = model();
    PatBodyPose.apply(model, frame(1.0f, arm, 1.0f, 1.0f, new Vector3f(-4, 14, -8)));
    assertEquals(model.rightArm.pitch, model.rightSleeve.pitch, EPSILON);
    assertEquals(bendOf(model.rightArm), bendOf(model.rightSleeve), EPSILON);
    assertEquals(bendOf(model.leftLeg), bendOf(model.leftPants), EPSILON);
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
    PatBodyPose.apply(model, frame(1.0f, arm, 0.0f, 0.0f, new Vector3f(-4, 14, -8)));
    assertEquals(0.3f, model.rightArm.pitch, EPSILON);
    assertEquals(-0.2f, model.leftLeg.pitch, EPSILON);
    assertEquals(0.1f, model.head.pitch, EPSILON);
    assertEquals(12.0f, model.leftLeg.pivotY, EPSILON);
    assertEquals(0.0f, bendOf(model.leftLeg), EPSILON);
  }

  @ParameterizedTest(name = "dog crown at {0},{1},{2}")
  @CsvSource({"0, 13, 12", "8, 13, 4", "-8, 13, 4", "0, 13, -12"})
  @DisplayName("the player's head never turns further than a real neck can")
  void theHeadTurnsOnlyAsFarAsANeckCan(final float x, final float y, final float z) {
    final Quaternionf look =
        PatBodyPose.lookAt(
            new Vector3f(0.0f, 0.0f, 0.0f), new Quaternionf(), new Vector3f(x, y, z));
    final Vector3f facing = look.transform(new Vector3f(0.0f, 0.0f, -1.0f));
    final float turn = (float) Math.toDegrees(Math.atan2(-facing.x, -facing.z));
    assertTrue(Math.abs(turn) <= 75.0f + 1.0e-3f, "turned " + turn);
  }
}
