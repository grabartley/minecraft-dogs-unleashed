package com.grahambartley.dogsunleashed.gesture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.gesture.PlayerPoseClip.Channel;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PlayerPoseClipsTest {

  private static final Path SHIPPED =
      Path.of("src/client/resources/assets/dogs-unleashed/player_poses/pat.json");
  private static final List<String> BONES =
      List.of(
          "hips",
          "torso",
          "head",
          "right_arm",
          "right_forearm",
          "left_arm",
          "left_forearm",
          "right_leg",
          "right_shin",
          "left_leg",
          "left_shin");

  private static PlayerPoseClip shipped(final String name) throws IOException {
    try (Reader reader = Files.newBufferedReader(SHIPPED, StandardCharsets.UTF_8)) {
      return PlayerPoseClip.parse(JsonParser.parseReader(reader).getAsJsonObject(), name);
    }
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {PlayerPoseClips.KNEEL_CLIP, PlayerPoseClips.STAND_CLIP})
  @DisplayName("each clip settles, strokes for exactly one pat, rises and ends at rest")
  void eachClipFitsThePat(final String name) throws IOException {
    final PlayerPoseClip clip = shipped(name);
    assertTrue(clip.length() > PatTimeline.STROKES_END, "the rise follows the strokes");
    for (final String bone : BONES) {
      assertEquals(0.0f, clip.sample(bone, Channel.ROTATION, 0.0f).length(), 1.0e-3f, bone);
      assertEquals(
          0.0f, clip.sample(bone, Channel.ROTATION, clip.length()).length(), 1.0e-3f, bone);
    }
    assertEquals(0.0f, clip.sample("hips", Channel.POSITION, clip.length()).length(), 1.0e-3f);
  }

  @ParameterizedTest(name = "{0}s")
  @ValueSource(floats = {0.6f, 1.0f, 1.4f, 1.8f, 2.1f})
  @DisplayName("the kneel clip stays down on one knee for the whole pat")
  void theKneelStaysDown(final float time) throws IOException {
    final PlayerPoseClip clip = shipped(PlayerPoseClips.KNEEL_CLIP);
    assertTrue(clip.sample("hips", Channel.POSITION, time).y < -3.5f);
    assertTrue(clip.sample("right_shin", Channel.ROTATION, time).x > 75.0f);
  }

  @ParameterizedTest(name = "{0}s")
  @ValueSource(floats = {0.6f, 1.0f, 1.4f, 1.8f, 2.1f})
  @DisplayName("the standing clip stays on its feet and rests the hand down beside it")
  void theStandStaysUp(final float time) throws IOException {
    final PlayerPoseClip clip = shipped(PlayerPoseClips.STAND_CLIP);
    final Vector3f arm = clip.sample("right_arm", Channel.ROTATION, time);
    assertTrue(clip.sample("hips", Channel.POSITION, time).y > -1.0f);
    assertTrue(arm.z > 20.0f, "the arm swings out to the dog's side, got " + arm.z);
    assertTrue(Math.abs(arm.x) < 60.0f, "the hand stays low, got " + arm.x);
  }

  @ParameterizedTest(name = "{0} {1}")
  @CsvSource({"pat, right_forearm", "pat, left_shin", "pat_tall, right_forearm"})
  @DisplayName("knees and elbows only fold the way real ones do")
  void jointsFoldOneWay(final String name, final String bone) throws IOException {
    final PlayerPoseClip clip = shipped(name);
    final boolean knee = bone.endsWith("shin");
    for (float time = 0.0f; time <= clip.length(); time += 0.05f) {
      final float bend = clip.sample(bone, Channel.ROTATION, time).x;
      assertTrue(knee ? bend >= -1.0f : bend <= 1.0f, bone + " at " + time + " is " + bend);
    }
  }
}
