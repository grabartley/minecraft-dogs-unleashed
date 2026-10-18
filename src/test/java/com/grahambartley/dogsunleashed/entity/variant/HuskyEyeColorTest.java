package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HuskyEyeColorTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "HAZEL_HAZEL, false, eyes.dogs-unleashed.husky.hazel_hazel",
    "BLUE_BLUE, false, eyes.dogs-unleashed.husky.blue_blue",
    "BLUE_HAZEL, true, eyes.dogs-unleashed.husky.blue_hazel",
    "HAZEL_BLUE, true, eyes.dogs-unleashed.husky.hazel_blue"
  })
  @DisplayName("only odd eyes count as heterochromatic, and each colour has its own name")
  void heterochromiaAndNames(
      final HuskyEyeColor eyes, final boolean heterochromatic, final String translationKey) {
    assertEquals(heterochromatic, eyes.heterochromatic());
    assertEquals(translationKey, eyes.translationKey());
  }

  @ParameterizedTest(name = "ordinal {0}")
  @CsvSource({"-1, HAZEL_HAZEL", "1, BLUE_BLUE", "3, HAZEL_BLUE", "4, HAZEL_HAZEL"})
  @DisplayName("ordinals out of range fall back to hazel")
  void fromOrdinalFallsBackToHazel(final int ordinal, final HuskyEyeColor expected) {
    assertEquals(expected, HuskyEyeColor.fromOrdinal(ordinal));
  }
}
