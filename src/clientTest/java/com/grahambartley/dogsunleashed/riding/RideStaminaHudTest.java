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

  @ParameterizedTest(name = "charge {0}, stamina {1} -> {2}")
  @CsvSource({"0,0.7,0.7", "0.4,0.7,0.4", "1,0.2,1"})
  @DisplayName("the bar shows the jump charge while charging and stamina otherwise")
  void shownFraction(final float charge, final float stamina, final float expected) {
    assertEquals(expected, RideStaminaHud.shownFraction(charge, stamina));
  }

  @ParameterizedTest(name = "rideable dog {0}, pulled sled {1} -> {2}")
  @CsvSource({"true,false,true", "false,false,false", "false,true,true", "true,true,true"})
  @DisplayName("the bar shows on a rideable dog or a sled with dogs hitched")
  void shows(final boolean rideableDog, final boolean pulledSled, final boolean expected) {
    assertEquals(expected, RideStaminaHud.shows(rideableDog, pulledSled));
  }
}
