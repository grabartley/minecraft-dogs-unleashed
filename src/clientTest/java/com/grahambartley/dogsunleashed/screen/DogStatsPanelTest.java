package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogStatsPanelTest {

  @Test
  @DisplayName("the stat maxima match the strongest breed in each category")
  void statMaximaMatchStrongestBreeds() {
    assertEquals(25.0, DogStatsPanel.maxHealthAcrossBreeds());
    assertEquals(0.32, DogStatsPanel.maxSpeedAcrossBreeds());
    assertEquals(5.0, DogStatsPanel.maxAttackAcrossBreeds());
  }

  @ParameterizedTest(name = "value {0} of max {1} is fraction {2}")
  @CsvSource({
    "25.0, 25.0, 1.0",
    "12.5, 25.0, 0.5",
    "0.0, 25.0, 0.0",
    "30.0, 25.0, 1.0",
    "-1.0, 25.0, 0.0",
    "10.0, 0.0, 0.0",
  })
  @DisplayName("fractions clamp between zero and one and survive a zero max")
  void fractionsClampAndSurviveZeroMax(final double value, final double max, final float expected) {
    assertEquals(expected, DogStatsPanel.fraction(value, max), 1e-6f);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("every breed's stat fractions stay within the bar")
  void everyBreedStaysWithinBar(final UnleashedDogBreed breed) {
    final float healthFraction =
        DogStatsPanel.fraction(
            breed.attributes().maxHealth(), DogStatsPanel.maxHealthAcrossBreeds());
    final float speedFraction =
        DogStatsPanel.fraction(
            breed.attributes().movementSpeed(), DogStatsPanel.maxSpeedAcrossBreeds());
    final float attackFraction =
        DogStatsPanel.fraction(
            breed.attributes().attackDamage(), DogStatsPanel.maxAttackAcrossBreeds());

    for (final float fraction : new float[] {healthFraction, speedFraction, attackFraction}) {
      assertTrue(fraction > 0.0f && fraction <= 1.0f, "fraction " + fraction + " is out of range");
      final int fill = DogStatsPanel.fillWidth(fraction, DogStatsPanel.BAR_WIDTH);
      assertTrue(fill > 0 && fill <= DogStatsPanel.BAR_WIDTH, "fill " + fill + " is out of range");
    }
  }

  @ParameterizedTest(name = "fraction {0} of {1}px fills {2}px")
  @CsvSource({
    "1.0, 70, 70",
    "0.5, 70, 35",
    "0.0, 70, 0",
    "0.014, 70, 1",
  })
  @DisplayName("fill widths round to the nearest pixel")
  void fillWidthsRoundToNearestPixel(final float fraction, final int barWidth, final int expected) {
    assertEquals(expected, DogStatsPanel.fillWidth(fraction, barWidth));
  }
}
