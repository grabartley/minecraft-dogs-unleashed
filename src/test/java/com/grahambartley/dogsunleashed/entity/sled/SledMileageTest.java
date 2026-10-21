package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledMileageTest {

  @ParameterizedTest(name = "{0} + {1} in snow {2} -> {3}")
  @CsvSource({
    "0.0, 0.5, true, 0.5",
    "999.8, 0.4, true, 1000.2",
    "10.0, 0.5, false, 10.0",
    "10.0, 0.0, true, 10.0",
    "10.0, -1.0, true, 10.0"
  })
  @DisplayName("only distance covered in snowy biomes counts")
  void add(final double before, final double blocks, final boolean snowy, final double expected) {
    assertEquals(expected, SledMileage.add(before, blocks, snowy), 1.0e-9);
  }

  @ParameterizedTest(name = "{0} blocks in a tick count as {1}")
  @CsvSource({"0.3, 0.3", "0.672, 0.672", "5.0, 0.672", "100.0, 0.672"})
  @DisplayName("a tick never counts more than the fastest sprint, so teleports add nothing extra")
  void capped(final double blocks, final double expected) {
    assertEquals(expected, SledMileage.capped(blocks), 1.0e-6);
  }
}
