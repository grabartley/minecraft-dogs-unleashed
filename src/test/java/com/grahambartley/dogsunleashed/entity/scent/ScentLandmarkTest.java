package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScentLandmarkTest {

  @ParameterizedTest(name = "({0}, {1}) -> column ({2}, {3})")
  @CsvSource({
    "10.0, 20.0, 10, 20",
    "10.9, 20.4, 10, 20",
    "-0.5, -15.2, -1, -16",
    "0.0, -0.1, 0, -1"
  })
  @DisplayName("a map position lands in the block column that contains it")
  void floorsToTheContainingColumn(
      final double x, final double z, final int columnX, final int columnZ) {
    assertEquals(new ScentLandmark(columnX, columnZ), ScentLandmark.at(x, z));
  }

  @Test
  @DisplayName("the nearest mark wins whatever order the marks come in")
  void picksTheNearestMark() {
    final ScentLandmark far = new ScentLandmark(200, 0);
    final ScentLandmark near = new ScentLandmark(-30, 40);
    final ScentLandmark middle = new ScentLandmark(0, 120);
    assertEquals(Optional.of(near), ScentLandmark.nearest(List.of(far, near, middle), 0.0, 0.0));
    assertEquals(Optional.of(near), ScentLandmark.nearest(List.of(middle, far, near), 0.0, 0.0));
  }

  @Test
  @DisplayName("no marks means nothing to follow")
  void noMarksIsEmpty() {
    assertTrue(ScentLandmark.nearest(List.of(), 5.0, 5.0).isEmpty());
  }

  @ParameterizedTest(name = "from ({0}, {1}) is {2}")
  @CsvSource({"3.5, 4.5, 0.0", "0.5, 0.5, 25.0", "6.5, 8.5, 25.0"})
  @DisplayName("distance is measured to the middle of the column")
  void measuresToTheColumnCentre(final double fromX, final double fromZ, final double expected) {
    assertEquals(expected, new ScentLandmark(3, 4).squaredDistanceTo(fromX, fromZ), 1.0E-9);
  }
}
