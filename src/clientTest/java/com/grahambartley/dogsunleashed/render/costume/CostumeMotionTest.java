package com.grahambartley.dogsunleashed.render.costume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CostumeMotionTest {

  @ParameterizedTest(name = "on ground {0}, in water {1}, riding {2} -> {3}")
  @CsvSource({
    "true, false, false, ''",
    "false, false, false, jump",
    "false, false, true, ''",
    "true, true, false, swim",
    "false, true, false, swim",
    "false, true, true, swim"
  })
  @DisplayName("costume parts swim in water and jump in the air, but not while riding")
  void picksTheBodyClip(
      final boolean onGround, final boolean inWater, final boolean riding, final String clip) {
    assertEquals(
        clip.isEmpty() ? Optional.empty() : Optional.of(clip),
        CostumeMotion.bodyClip(onGround, inWater, riding));
  }

  @ParameterizedTest(name = "{0} ticks after a bark -> barking {1}")
  @CsvSource({
    "0.0, true",
    "0.5, true",
    "9.99, true",
    "10.0, false",
    "500.0, false",
    "-1.0, false",
    "1073741824.0, false"
  })
  @DisplayName("costume parts play the bark clip for half a second after each bark")
  void barksForHalfASecond(final float ticksSinceBark, final boolean barking) {
    assertEquals(barking, CostumeMotion.barking(ticksSinceBark));
  }
}
