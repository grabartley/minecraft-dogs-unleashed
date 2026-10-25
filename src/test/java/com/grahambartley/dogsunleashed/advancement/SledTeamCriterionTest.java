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

class SledTeamCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:sled_team")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:sled_team", SledTeamCriterion.ID.toString());
  }

  @ParameterizedTest(name = "team of {0} against a minimum of {1} -> {2}")
  @CsvSource({"6, 6, true", "5, 6, false", "1, 1, true", "3, , true"})
  @DisplayName("a team matches once it reaches the minimum size")
  void matches(final int team, final Integer minimum, final boolean expected) {
    assertEquals(
        expected,
        new SledTeamCriterion.Conditions(Optional.empty(), Optional.ofNullable(minimum))
            .matches(team));
  }

  @ParameterizedTest(name = "step {0}")
  @ValueSource(ints = {1, 2, 3, 4, 5, 6})
  @DisplayName("Full Team has one progress step per dog and keeps its old criterion in each")
  void advancementStepsUp(final int step) throws IOException {
    try (InputStream in =
        SledTeamCriterionTest.class.getResourceAsStream(
            "/data/dogs-unleashed/advancement/full_team.json")) {
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
                              .get("min_team")
                              .getAsInt()
                          == step)
              .count();
      assertEquals(1, matching);
      assertEquals(6, json.getAsJsonArray("requirements").size());
      json.getAsJsonArray("requirements")
          .forEach(
              group -> assertTrue(group.getAsJsonArray().contains(new JsonPrimitive("full_team"))));
    }
  }
}
