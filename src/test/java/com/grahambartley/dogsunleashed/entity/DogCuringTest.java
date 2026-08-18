package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.random.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogCuringTest {

  private static final int SAMPLES = 2000;

  @ParameterizedTest(name = "seed {0} rolls a timer inside the vanilla conversion range")
  @ValueSource(longs = {1L, 42L, 9001L})
  @DisplayName("the conversion timer stays inside the vanilla zombie villager range")
  void conversionTimerStaysInTheVanillaRange(final long seed) {
    final Random random = Random.create(seed);
    for (int i = 0; i < SAMPLES; i++) {
      final int ticks = DogCuring.rollConversionTicks(random);
      assertTrue(
          ticks >= DogCuring.MIN_CONVERSION_TICKS && ticks <= DogCuring.MAX_CONVERSION_TICKS,
          "rolled " + ticks + " outside the conversion range");
    }
  }

  @Test
  @DisplayName("the conversion timer reaches both ends of its range")
  void conversionTimerCoversItsWholeRange() {
    final Random random = Random.create(7L);
    int lowest = Integer.MAX_VALUE;
    int highest = Integer.MIN_VALUE;
    for (int i = 0; i < SAMPLES * 10; i++) {
      final int ticks = DogCuring.rollConversionTicks(random);
      lowest = Math.min(lowest, ticks);
      highest = Math.max(highest, ticks);
    }
    assertEquals(DogCuring.MIN_CONVERSION_TICKS, lowest, "lowest roll");
    assertEquals(DogCuring.MAX_CONVERSION_TICKS, highest, "highest roll");
  }

  @ParameterizedTest(name = "a saved timer of {0} restores as {1}")
  @CsvSource({"-1, 0", "0, 0", "1, 1", "4200, 4200"})
  @DisplayName("a corrupt or negative saved timer restores as no conversion in progress")
  void savedTimerIsSanitized(final int saved, final int expected) {
    assertEquals(expected, DogCuring.sanitizedRemainingTicks(saved));
  }
}
