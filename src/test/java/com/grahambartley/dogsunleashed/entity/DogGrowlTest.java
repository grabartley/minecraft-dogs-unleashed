package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogGrowlTest {

  @Test
  @DisplayName("a growl repeats at most every three seconds")
  void repeatInterval() {
    assertEquals(60, DogGrowl.REPEAT_TICKS);
  }

  @ParameterizedTest(name = "cooldown {0}, in melee {1} -> {2}")
  @CsvSource({"0,false,true", "1,false,false", "0,true,false", "30,true,false", "-1,false,true"})
  @DisplayName("an angry wolf growls again once the cooldown is over and it is not biting")
  void growlsAgain(final int cooldown, final boolean inMelee, final boolean expected) {
    assertEquals(expected, DogGrowl.growlsAgain(cooldown, inMelee));
  }
}
