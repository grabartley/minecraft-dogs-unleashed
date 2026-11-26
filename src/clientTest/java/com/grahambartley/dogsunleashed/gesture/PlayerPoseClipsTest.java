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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PlayerPoseClipsTest {

  private static final Path SHIPPED =
      Path.of("src/client/resources/assets/dogs-unleashed/player_poses/pat.json");

  private static PlayerPoseClip shipped() throws IOException {
    try (Reader reader = Files.newBufferedReader(SHIPPED, StandardCharsets.UTF_8)) {
      return PlayerPoseClip.parse(
          JsonParser.parseReader(reader).getAsJsonObject(), PlayerPoseClips.PAT_CLIP);
    }
  }

  @Test
  @DisplayName("the clip kneels, strokes for exactly one pat, then rises")
  void theClipFitsThePat() throws IOException {
    final PlayerPoseClip clip = shipped();
    assertTrue(clip.length() > PatTimeline.STROKES_END, "the rise follows the strokes");
    assertEquals(0.0f, clip.sample("hips", Channel.POSITION, 0.0f).length(), 1.0e-4f);
    assertEquals(0.0f, clip.sample("hips", Channel.POSITION, clip.length()).length(), 1.0e-4f);
  }

  @ParameterizedTest(name = "{0}s")
  @ValueSource(floats = {0.4f, 0.8f, 1.2f, 1.6f, 1.9f})
  @DisplayName("the player stays knelt for the whole pat")
  void thePlayerStaysKnelt(final float time) throws IOException {
    assertTrue(shipped().sample("hips", Channel.POSITION, time).y < -5.0f);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {"hips", "torso", "head", "right_arm", "left_arm", "right_leg", "left_leg"})
  @DisplayName("every bone the pose reads is keyed")
  void everyBoneIsKeyed(final String bone) throws IOException {
    final PlayerPoseClip clip = shipped();
    final float sum =
        clip.sample(bone, Channel.ROTATION, 1.0f).length()
            + clip.sample(bone, Channel.POSITION, 1.0f).length();
    assertTrue(sum > 0.0f, bone + " moves mid pat");
  }
}
