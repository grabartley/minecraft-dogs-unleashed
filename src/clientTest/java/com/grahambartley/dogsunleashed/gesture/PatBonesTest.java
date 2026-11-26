package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.gesture.PatSkeleton.Bones;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PatBonesTest {

  private static final float EPSILON = 1.0e-4f;

  private static PlayerPoseClip clip(final String hipsY, final String knee, final String arm) {
    return PlayerPoseClip.parse(
        JsonParser.parseString(
                """
                {"animations": {"c": {"animation_length": 1, "bones": {
                  "hips": {"position": {"0": [0, %s, 0]}},
                  "right_shin": {"rotation": {"0": [%s, 0, 0]}},
                  "right_arm": {"rotation": {"0": [%s, 0, 0]}}
                }}}}
                """
                    .formatted(hipsY, knee, arm))
            .getAsJsonObject(),
        "c");
  }

  @ParameterizedTest(name = "hips {0}, shin {1}, arm {2}")
  @CsvSource({"-4, 85, -50", "0, 0, 0", "-0.5, 10, 30"})
  @DisplayName("the clip's numbers become drops, bends and turns in the model's own units")
  void theClipBecomesBones(final float hips, final float knee, final float arm) {
    final Bones bones = PatBones.sample(clip("" + hips, "" + knee, "" + arm), 0.0f);
    assertEquals(-hips, bones.hipsOffset().y, EPSILON);
    assertEquals((float) Math.toRadians(knee), bones.rightKnee(), EPSILON);
    assertEquals((float) Math.toRadians(arm), PatSkeleton.of(bones).rightArm().angles().x, EPSILON);
  }

  @ParameterizedTest(name = "blend {0}")
  @CsvSource({"0, 0", "0.5, 2", "1, 4"})
  @DisplayName("two clips blend smoothly from one to the other")
  void clipsBlend(final float t, final float drop) {
    final Bones stand = PatBones.sample(clip("0", "0", "0"), 0.0f);
    final Bones kneel = PatBones.sample(clip("-4", "90", "-40"), 0.0f);
    final Bones blended = PatBones.blend(stand, kneel, t);
    assertEquals(drop, blended.hipsOffset().y, EPSILON);
    assertEquals(t * (float) Math.toRadians(90), blended.rightKnee(), EPSILON);
  }
}
