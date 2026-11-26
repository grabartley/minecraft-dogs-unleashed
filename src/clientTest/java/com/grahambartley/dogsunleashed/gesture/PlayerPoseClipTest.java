package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.gesture.PlayerPoseClip.Channel;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerPoseClipTest {

  private static final String FILE =
      """
      {"animations": {"pat": {"animation_length": 2.0, "bones": {
        "hips": {"position": {"0.0": {"vector": [0, 0, 0]}, "1.0": {"vector": [0, -6, 0]}}},
        "torso": {"rotation": {"0.5": [10, 0, 0], "1.5": [20, 4, -2]}}
      }}}}
      """;
  private static final PlayerPoseClip CLIP =
      PlayerPoseClip.parse(JsonParser.parseString(FILE).getAsJsonObject(), "pat");

  @ParameterizedTest(name = "hips at {0}s -> y {1}")
  @CsvSource({"-1, 0", "0, 0", "0.25, -1.5", "0.5, -3", "1.0, -6", "1.7, -6"})
  @DisplayName("a track holds its ends and blends straight between keys")
  void aTrackBlendsBetweenKeys(final float time, final float y) {
    assertEquals(y, CLIP.sample("hips", Channel.POSITION, time).y, 1.0e-5f);
  }

  @ParameterizedTest(name = "torso at {0}s -> {1}, {2}, {3}")
  @CsvSource({"0, 10, 0, 0", "1.0, 15, 2, -1", "2.0, 20, 4, -2"})
  @DisplayName("bare arrays and vector objects both read as keys")
  void bothKeyFormsRead(final float time, final float x, final float y, final float z) {
    final Vector3f value = CLIP.sample("torso", Channel.ROTATION, time);
    assertEquals(x, value.x, 1.0e-5f);
    assertEquals(y, value.y, 1.0e-5f);
    assertEquals(z, value.z, 1.0e-5f);
  }

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({"head, ROTATION", "hips, ROTATION", "torso, POSITION"})
  @DisplayName("a channel the clip leaves out rests at zero")
  void aMissingChannelRests(final String bone, final Channel channel) {
    assertEquals(0.0f, CLIP.sample(bone, channel, 1.0f).length(), 1.0e-6f);
  }
}
