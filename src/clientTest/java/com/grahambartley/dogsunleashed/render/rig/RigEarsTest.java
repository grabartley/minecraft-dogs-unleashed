package com.grahambartley.dogsunleashed.render.rig;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class RigEarsTest {

  private static final Set<String> NATIVE = Set.of("ear_l", "ear_r");
  private static final RigEars DROP_WITH_PRICKED =
      RigEars.parse(
          JsonParser.parseString(
              """
              {
                "shape": "drop",
                "variants": {
                  "pricked": ["pricked_l", "pricked_r"],
                  "rose": ["rose_l", "rose_r"]
                }
              }
              """));

  private static final Set<String> VARIANTS = Set.of("pricked_l", "pricked_r", "rose_l", "rose_r");

  static Stream<Arguments> wantedShapes() {
    return Stream.of(
        Arguments.of("no wish keeps the rig's own ears", Optional.empty(), Set.of(), VARIANTS),
        Arguments.of(
            "the rig's own shape keeps its own ears", Optional.of("drop"), Set.of(), VARIANTS),
        Arguments.of(
            "a shape the rig lacks keeps its own ears", Optional.of("folded"), Set.of(), VARIANTS),
        Arguments.of(
            "a variant swaps in whole and hides the own ears and the other variants",
            Optional.of("pricked"),
            Set.of(),
            Set.of("ear_l", "ear_r", "rose_l", "rose_r")),
        Arguments.of(
            "headwear over the own ears still hides them",
            Optional.empty(),
            Set.of("ear_l"),
            Set.of("ear_l", "pricked_l", "pricked_r", "rose_l", "rose_r")),
        Arguments.of(
            "headwear that hides ears also hides a swapped pair",
            Optional.of("pricked"),
            Set.of("ear_l"),
            Set.of("ear_l", "ear_r", "pricked_l", "pricked_r", "rose_l", "rose_r")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wantedShapes")
  @DisplayName("a wanted ear shape and any headwear decide which ear bones hide")
  void wantedShapeDecidesTheEars(
      final String name,
      final Optional<String> wanted,
      final Set<String> headwear,
      final Set<String> hidden) {
    assertEquals(hidden, DROP_WITH_PRICKED.hiddenFor(wanted, NATIVE, headwear));
  }

  @ParameterizedTest(name = "headwear {0}")
  @MethodSource("headwear")
  @DisplayName("a rig with no ear variants hides exactly what the headwear hides")
  void noVariantsHideOnlyHeadwear(final Set<String> headwear) {
    assertEquals(headwear, RigEars.NONE.hiddenFor(Optional.of("pricked"), NATIVE, headwear));
  }

  static Stream<Set<String>> headwear() {
    return Stream.of(Set.of(), Set.of("ear_l"), NATIVE);
  }

  @Test
  @DisplayName("the shape and every variant read from the profile")
  void readsShapeAndVariants() {
    assertEquals(Optional.of("drop"), DROP_WITH_PRICKED.shape());
    assertEquals(
        Map.of("pricked", Set.of("pricked_l", "pricked_r"), "rose", Set.of("rose_l", "rose_r")),
        DROP_WITH_PRICKED.variants());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "\"drop\"",
        "[]",
        "{\"shape\": 3}",
        "{\"variants\": {\"pricked\": []}}",
        "{\"variants\": {\"pricked\": [3, {}]}}"
      })
  @DisplayName("malformed ears read as no shape and no variants, so nothing is hidden")
  void malformedEarsAreEmpty(final String json) {
    final RigEars ears = RigEars.parse(JsonParser.parseString(json));

    assertEquals(Optional.empty(), ears.shape());
    assertEquals(Map.of(), ears.variants());
    assertEquals(Set.of(), ears.hiddenFor(Optional.of("pricked"), NATIVE, Set.of()));
  }
}
