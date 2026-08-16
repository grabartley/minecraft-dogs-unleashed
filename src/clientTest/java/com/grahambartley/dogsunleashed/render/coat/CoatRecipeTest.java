package com.grahambartley.dogsunleashed.render.coat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoatRecipeTest {

  private static final String JSON =
      """
      {
        "pigments": {"pigment2": "#8D683D", "pigment1": "#F6F1EB"},
        "baseScale": {"pigment1": 128, "pigment2": 101}
      }
      """;

  private static CoatRecipe parsed() {
    return CoatRecipe.fromJson(JsonParser.parseString(JSON).getAsJsonObject());
  }

  @Test
  @DisplayName("slots come back in a stable order regardless of how the file was written")
  void slotsAreStablyOrdered() {
    assertEquals(List.of("pigment1", "pigment2"), parsed().slots());
  }

  @Test
  @DisplayName("hex pigments and their base scales are read off the recipe")
  void pigmentsAndScalesAreParsed() {
    final CoatRecipe recipe = parsed();
    assertEquals(0xF6F1EB, recipe.pigment("pigment1"));
    assertEquals(0x8D683D, recipe.pigment("pigment2"));
    assertEquals(128, recipe.baseScaleOf("pigment1"));
    assertEquals(101, recipe.baseScaleOf("pigment2"));
  }

  @Test
  @DisplayName("a missing base scale falls back to the encoding default rather than zero")
  void missingBaseScaleFallsBackToDefault() {
    final JsonObject json =
        JsonParser.parseString("{\"pigments\": {\"pigment1\": \"#FFFFFF\"}, \"baseScale\": {}}")
            .getAsJsonObject();
    assertEquals(CoatRecipe.DEFAULT_BASE_SCALE, CoatRecipe.fromJson(json).baseScaleOf("pigment1"));
  }

  @ParameterizedTest(name = "index {0} -> {1}")
  @MethodSource("clampedIndices")
  @DisplayName(
      "asking past the last slot clamps rather than failing, so shorter coats still donate")
  void pigmentAtClampsToLastSlot(final int index, final int expected) {
    assertEquals(expected, parsed().pigmentAt(index));
  }

  static Stream<Arguments> clampedIndices() {
    return Stream.of(
        Arguments.of(0, 0xF6F1EB),
        Arguments.of(1, 0x8D683D),
        Arguments.of(2, 0x8D683D),
        Arguments.of(9, 0x8D683D));
  }

  @Test
  @DisplayName("an empty recipe donates white rather than black, so it cannot darken a blend")
  void emptyRecipeDonatesWhite() {
    final CoatRecipe empty = new CoatRecipe(List.of(), Map.of(), Map.of());
    assertEquals(0xFFFFFF, empty.pigmentAt(0));
  }
}
