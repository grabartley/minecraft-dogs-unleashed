package com.grahambartley.dogsunleashed.entity.trick;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;
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
  @DisplayName("tricks take wheel slots after explore and deliver, with no clashes")
  void wheelIdsAreDistinctAndAfterTheFixedActions() {
    assertEquals(
        DogTrick.values().length,
        Arrays.stream(DogTrick.values()).mapToInt(DogTrick::wheelActionId).distinct().count());
    assertTrue(Arrays.stream(DogTrick.values()).allMatch(trick -> trick.wheelActionId() > 1));
  }
}
