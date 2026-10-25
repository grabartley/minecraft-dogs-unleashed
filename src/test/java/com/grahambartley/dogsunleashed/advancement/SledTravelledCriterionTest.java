package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SledTravelledCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:sled_travelled")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:sled_travelled", SledTravelledCriterion.ID.toString());
  }

  @ParameterizedTest(name = "{0} blocks against a minimum of {1} -> {2}")
  @CsvSource({
    "1000.0, 1000.0, true",
    "999.9, 1000.0, false",
    "1200.5, 1000.0, true",
    "1.0, , true"
  })
  @DisplayName("distance matches once it reaches the minimum")
  void matches(final double blocks, final Double minimum, final boolean expected) {
    assertEquals(
        expected,
        new SledTravelledCriterion.Conditions(Optional.empty(), Optional.ofNullable(minimum))
            .matches(blocks));
  }

  @ParameterizedTest(name = "step {0}")
  @ValueSource(doubles = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000})
  @DisplayName(
      "Sleigh All Day has one progress step per 100 blocks and keeps its old criterion in each")
  void advancementStepsUp(final double step) throws IOException {
    try (InputStream in =
        SledTravelledCriterionTest.class.getResourceAsStream(
            "/data/dogs-unleashed/advancement/mush.json")) {
      final JsonObject json =
          JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
              .getAsJsonObject();
      final JsonObject criteria = json.getAsJsonObject("criteria");
      final long matching =
          criteria.entrySet().stream()
              .filter(
                  entry ->
                      entry
                              .getValue()
                              .getAsJsonObject()
                              .getAsJsonObject("conditions")
                              .get("min_blocks")
                              .getAsDouble()
                          == step)
              .count();
      assertEquals(1, matching);
      assertEquals(10, json.getAsJsonArray("requirements").size());
      json.getAsJsonArray("requirements")
          .forEach(
              group -> assertTrue(group.getAsJsonArray().contains(new JsonPrimitive("travelled"))));
    }
  }
}
