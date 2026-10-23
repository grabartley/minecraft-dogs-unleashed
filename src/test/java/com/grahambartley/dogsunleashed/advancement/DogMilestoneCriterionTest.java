package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogMilestoneCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:dog_milestone")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_milestone", DogMilestoneCriterion.ID.toString());
  }

  static Stream<Arguments> milestonePairs() {
    return Arrays.stream(DogMilestone.values())
        .flatMap(
            wanted ->
                Arrays.stream(DogMilestone.values())
                    .map(reached -> Arguments.of(wanted, reached, wanted == reached)));
  }

  @ParameterizedTest(name = "wants {0}, reached {1} -> {2}")
  @MethodSource("milestonePairs")
  @DisplayName("conditions match only the milestone they name")
  void conditionsMatchOnlyTheirMilestone(
      final DogMilestone wanted, final DogMilestone reached, final boolean expected) {
    assertEquals(
        expected, new DogMilestoneCriterion.Conditions(Optional.empty(), wanted).matches(reached));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogMilestone.class)
  @DisplayName("conditions round-trip through the advancement json codec")
  void conditionsRoundTripThroughCodec(final DogMilestone milestone) {
    final DogMilestoneCriterion.Conditions conditions =
        new DogMilestoneCriterion.Conditions(Optional.empty(), milestone);
    final var json =
        DogMilestoneCriterion.Conditions.CODEC
            .encodeStart(JsonOps.INSTANCE, conditions)
            .getOrThrow();
    assertEquals(milestone.asString(), json.getAsJsonObject().get("milestone").getAsString());
    assertEquals(
        conditions,
        DogMilestoneCriterion.Conditions.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
  }
}
