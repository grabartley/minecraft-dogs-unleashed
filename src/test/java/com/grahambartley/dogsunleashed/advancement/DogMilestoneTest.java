package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogMilestoneTest {

  @ParameterizedTest(name = "{0} is written as {1}")
  @CsvSource({
    "WHISTLE_POINT, whistle_point",
    "GEAR_EQUIPPED, gear_equipped",
    "COLLAR_DYED, collar_dyed",
    "SCENT_FOUND, scent_found"
  })
  @DisplayName("each milestone reads and writes as its json id")
  void readsAndWritesItsJsonId(final DogMilestone milestone, final String id) {
    assertEquals(id, milestone.asString());
    assertEquals(
        milestone, DogMilestone.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive(id)).getOrThrow());
  }
}
