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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogTrickTest {

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
}
