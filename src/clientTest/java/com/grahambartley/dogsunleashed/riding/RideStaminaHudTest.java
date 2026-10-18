package com.grahambartley.dogsunleashed.riding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RideStaminaHudTest {

  @ParameterizedTest(name = "{0} -> {1} pixels")
  @CsvSource({"0,0", "0.5,91", "1,182", "-0.2,0", "1.4,182"})
  @DisplayName("the bar fills in proportion to stamina, never past its ends")
  void fillWidth(final float fraction, final int expected) {
    assertEquals(expected, RideStaminaHud.fillWidth(fraction));
  }
}
