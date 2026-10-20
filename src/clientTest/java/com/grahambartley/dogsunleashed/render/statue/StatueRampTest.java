package com.grahambartley.dogsunleashed.render.statue;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class StatueRampTest {

  @ParameterizedTest(name = "rgb({0}, {1}, {2}) has luma {3}")
  @CsvSource({
    "0, 0, 0, 0.0",
    "255, 255, 255, 1.0",
    "255, 0, 0, 0.2126",
    "0, 255, 0, 0.7152",
    "0, 0, 255, 0.0722"
  })
  @DisplayName("luma uses the Rec. 709 weights")
  void lumaUsesRec709(final int red, final int green, final int blue, final double luma) {
    assertEquals(luma, StatueRamp.luma(red, green, blue), 1e-9);
  }

  static Stream<Arguments> coats() {
    return Stream.of(
        Arguments.of(new double[] {0.0, 1.0}, new int[] {0, 15}),
        Arguments.of(new double[] {0.1, 0.5}, new int[] {1, 11}),
        Arguments.of(new double[] {0.5, 0.5, 0.5}, new int[] {8, 8, 8}),
        Arguments.of(new double[] {0.2, 0.2}, new int[] {3, 3}),
        Arguments.of(new double[] {0.8, 0.8}, new int[] {12, 12}),
        Arguments.of(new double[0], new int[0]));
  }

  @ParameterizedTest(name = "{0} maps to steps {1}")
  @MethodSource("coats")
  @DisplayName("each pixel lands on the ramp halfway between its own and its stretched brightness")
  void stepsMixOwnAndStretchedBrightness(final double[] lumas, final int[] steps) {
    assertArrayEquals(steps, StatueRamp.steps(lumas), Arrays.toString(lumas));
  }

  @Test
  @DisplayName("a single bright or dark speck does not set the stretch for the whole coat")
  void outliersAreIgnoredByThePercentiles() {
    final double[] lumas = new double[101];
    for (int i = 0; i < lumas.length; i++) {
      lumas[i] = 0.3 + 0.4 * i / 100.0;
    }
    lumas[0] = 0.0;
    lumas[100] = 1.0;

    final int[] steps = StatueRamp.steps(lumas);

    assertEquals(0, steps[0]);
    assertEquals(15, steps[100]);
    assertEquals(2, steps[2], "the darkest ordinary pixel sits near the bottom of the ramp");
    assertEquals(13, steps[98], "the brightest ordinary pixel sits near the top of the ramp");
  }

  @Test
  @DisplayName("percentiles round to the nearest rank")
  void percentileUsesNearestRank() {
    final double[] sorted = {0.0, 0.25, 0.5, 0.75, 1.0};
    assertEquals(0.0, StatueRamp.percentile(sorted, 0.02));
    assertEquals(1.0, StatueRamp.percentile(sorted, 0.98));
    assertEquals(0.5, StatueRamp.percentile(sorted, 0.5));
  }
}
