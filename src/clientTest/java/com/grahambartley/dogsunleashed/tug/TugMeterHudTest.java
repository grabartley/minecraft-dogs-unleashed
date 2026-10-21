package com.grahambartley.dogsunleashed.tug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TugMeterHudTest {

  @ParameterizedTest(name = "balance {0} -> {1} pixels")
  @CsvSource({"-1,0", "-0.5,46", "0,91", "0.5,137", "1,182", "-3,0", "2,182"})
  @DisplayName("your side of the bar starts half full and grows as you win")
  void playerWidth(final float balance, final int expected) {
    assertEquals(expected, TugMeterHud.playerWidth(balance));
  }

  @ParameterizedTest(name = "tugging player {0}, viewer {1} -> {2}")
  @CsvSource({"7,7,true", "7,8,false", "-1,7,false", "-1,-1,false"})
  @DisplayName("only the player on the other end of the rope sees the bar")
  void shows(final int tuggingPlayerId, final int viewerId, final boolean expected) {
    assertEquals(expected, TugMeterHud.shows(tuggingPlayerId, viewerId));
  }
}
