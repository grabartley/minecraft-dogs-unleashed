package com.grahambartley.dogsunleashed.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DimensionLabelFormatterTest {

  static Stream<Arguments> dimensionLabels() {
    return Stream.of(
        Arguments.of("overworld -> Overworld", "minecraft:overworld", "Overworld"),
        Arguments.of("nether -> Nether", "minecraft:the_nether", "Nether"),
        Arguments.of("end -> The End", "minecraft:the_end", "The End"),
        Arguments.of(
            "modded strips namespace and swaps underscores",
            "twilightforest:twilight_forest",
            "twilight forest"),
        Arguments.of("modded single-word path", "aether:the_aether", "the aether"),
        Arguments.of("path without namespace", "custom_realm", "custom realm"),
        Arguments.of("empty string -> empty", "", ""),
        Arguments.of("null -> empty", null, ""));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dimensionLabels")
  @DisplayName("format maps vanilla dimensions to friendly names and falls back for the rest")
  void formatMapsDimensionToLabel(
      final String label, final String dimensionId, final String expected) {
    assertEquals(expected, DimensionLabelFormatter.format(dimensionId));
  }
}
