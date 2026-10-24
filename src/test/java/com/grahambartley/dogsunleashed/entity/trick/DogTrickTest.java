package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.dogsunleashed.entity.delivery.DogDeliveries;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogTrickTest {

  private static final Path ANIMATIONS =
      Path.of("src/client/resources/assets/dogs-unleashed/animations");

  @ParameterizedTest(name = "{0} is saved as {1}, plays {2} and sits in wheel slot {3}")
  @CsvSource({
    "BEG, beg, beg, 2",
    "ROLL_OVER, roll_over, roll_over, 3",
    "PLAY_DEAD, play_dead, play_dead, 4"
  })
  @DisplayName("each trick has its saved id, clip and wheel slot")
  void tricksCarryTheirIds(
      final DogTrick trick, final String id, final String clip, final int wheelId) {
    assertEquals(id, trick.serializedId());
    assertEquals(clip, trick.animationKey());
    assertEquals(wheelId, trick.wheelActionId());
    assertEquals("trick.dogs-unleashed." + id, trick.translationKey());
  }

  @ParameterizedTest
  @EnumSource(DogTrick.class)
  @DisplayName("every trick reads back from its saved id")
  void roundTripsThroughSerializedId(final DogTrick trick) {
    assertEquals(Optional.of(trick), DogTrick.fromSerializedId(trick.serializedId()));
  }

  @Test
  @DisplayName("an unknown id reads as no trick")
  void unknownIdIsEmpty() {
    assertTrue(DogTrick.fromSerializedId("fetch_slippers").isEmpty());
  }

  @Test
  @DisplayName("tricks take their own wheel slots, clear of explore and deliver")
  void wheelIdsAreDistinctFromTheOtherActions() {
    final Set<Integer> ids =
        Arrays.stream(DogTrick.values()).map(DogTrick::wheelActionId).collect(Collectors.toSet());
    assertEquals(DogTrick.values().length, ids.size());
    assertFalse(ids.contains(DogExpeditions.WHEEL_ACTION_ID));
    assertFalse(ids.contains(DogDeliveries.WHEEL_ACTION_ID));
  }

  @Test
  @DisplayName("the Show Dog advancement asks for every trick")
  void showDogNeedsEveryTrick() throws IOException {
    try (InputStream in =
        DogTrickTest.class.getResourceAsStream("/data/dogs-unleashed/advancement/show_dog.json")) {
      final JsonObject json =
          JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
              .getAsJsonObject();
      assertEquals(
          DogTrick.values().length,
          json.getAsJsonObject("criteria")
              .getAsJsonObject("every_trick_learned")
              .getAsJsonObject("conditions")
              .get("min_learned")
              .getAsInt());
    }
  }

  @ParameterizedTest
  @EnumSource(DogTrick.class)
  @DisplayName("each trick lasts exactly as long as its longest clip on any rig")
  void lastsAsLongAsItsLongestClip(final DogTrick trick) throws IOException {
    final List<Double> seconds = clipSeconds(trick);
    assertFalse(seconds.isEmpty());
    final double longest = seconds.stream().mapToDouble(Double::doubleValue).max().orElseThrow();
    assertEquals((int) Math.ceil(longest * 20), trick.durationTicks());
  }

  @ParameterizedTest(name = "{0} started at {1} is running at {2}: {3}")
  @CsvSource({
    "BEG, 0, 0, true",
    "BEG, 0, 47, true",
    "BEG, 0, 48, false",
    "ROLL_OVER, 100, 175, true",
    "ROLL_OVER, 100, 176, false",
    "PLAY_DEAD, 1000, 1103, true",
    "PLAY_DEAD, 1000, 1104, false",
    "PLAY_DEAD, 1000, 999, false"
  })
  @DisplayName("a trick runs for its length from the tick it starts")
  void runsForItsLength(
      final DogTrick trick, final long startedAt, final long now, final boolean running) {
    assertEquals(running, trick.isRunning(startedAt, now));
  }

  @ParameterizedTest
  @EnumSource(DogTrick.class)
  @DisplayName("a trick that never started is not running")
  void neverStartedIsNotRunning(final DogTrick trick) {
    assertFalse(trick.isRunning(TrickLearningRules.NEVER, 0));
  }

  private static List<Double> clipSeconds(final DogTrick trick) throws IOException {
    try (Stream<Path> files = Files.list(ANIMATIONS)) {
      final List<Double> seconds = new ArrayList<>();
      for (final Path file : files.filter(f -> f.toString().endsWith(".animation.json")).toList()) {
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
          final JsonObject clips =
              JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("animations");
          if (clips.has(trick.animationKey())) {
            seconds.add(
                clips.getAsJsonObject(trick.animationKey()).get("animation_length").getAsDouble());
          }
        }
      }
      return seconds;
    }
  }
}
