package com.grahambartley.dogsunleashed.render.pawprint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PawPrintRulesTest {

  @ParameterizedTest(name = "moved ({0}, {1}) stamps: {2}")
  @CsvSource({"0, 0, false", "0.3, 0.3, false", "0.5, 0, true", "0, -0.5, true", "0.4, 0.4, true"})
  @DisplayName("a new print is stamped once the dog has moved a stride")
  void stampsAfterAStride(final double x, final double z, final boolean stamps) {
    assertEquals(stamps, PawPrintRules.stamps(x, z));
  }

  @ParameterizedTest(name = "a print {0} ticks old has alpha {1}")
  @CsvSource({
    "-1, 1.0",
    "0, 1.0",
    "150, 0.75",
    "300, 0.5",
    "599, 0.0016667",
    "600, 0.0",
    "900, 0.0"
  })
  @DisplayName("prints fade out evenly over thirty seconds")
  void fades(final long age, final float alpha) {
    assertEquals(alpha, PawPrintRules.alpha(age), 1.0e-5f);
  }

  @ParameterizedTest(name = "a print {0} ticks old is gone: {1}")
  @CsvSource({"0, false", "599, false", "600, true"})
  @DisplayName("prints are gone after thirty seconds")
  void expires(final long age, final boolean expired) {
    assertEquals(expired, PawPrintRules.expired(age));
  }

  @ParameterizedTest(name = "a dog {0} wide leaves {1} wide prints offset {2} left and right")
  @CsvSource({"0.6, 0.24, 0.108", "1.0, 0.4, 0.18", "0.3, 0.12, 0.054"})
  @DisplayName("print size and spacing scale with the dog")
  void scalesWithDog(final float width, final float size, final float offset) {
    assertEquals(size, PawPrintRules.size(width), 1.0e-5f);
    assertEquals(-offset, PawPrintRules.sideOffset(true, width), 1.0e-5f);
    assertEquals(offset, PawPrintRules.sideOffset(false, width), 1.0e-5f);
  }

  @ParameterizedTest(name = "{0} blocks squared away is drawn: {1}")
  @CsvSource({"0, true", "2304, true", "2305, false"})
  @DisplayName("prints far from the camera are skipped")
  void inRange(final double distanceSquared, final boolean drawn) {
    assertEquals(drawn, PawPrintRules.inRange(distanceSquared));
  }
}
