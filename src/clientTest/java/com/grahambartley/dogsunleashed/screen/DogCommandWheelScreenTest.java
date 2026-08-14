package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogCommandWheelScreenTest {

  private static final int SECTORS = 8;
  private static final double INNER = 40;
  private static final double OUTER = 100;

  static Stream<Arguments> deadzoneAndOutOfRangePoints() {
    return Stream.of(
        Arguments.of("wheel center", 0, 0),
        Arguments.of("inside deadzone", 20, 20),
        Arguments.of("just inside inner radius straight up", 0, -39),
        Arguments.of("just beyond outer radius straight up", 0, -101),
        Arguments.of("far outside the wheel", 500, 500));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("deadzoneAndOutOfRangePoints")
  @DisplayName("the hub deadzone and points beyond the outer radius select nothing")
  void deadzoneSelectsNothing(final String label, final double dx, final double dy) {
    assertEquals(-1, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  static Stream<Arguments> sectorCenters() {
    // Screen y grows downward, so clockwise from 12 o'clock: up, up-right, right, down-right,
    // down, down-left, left, up-left.
    return Stream.of(
        Arguments.of("12 o'clock", 0, -70, 0),
        Arguments.of("1:30", 50, -50, 1),
        Arguments.of("3 o'clock", 70, 0, 2),
        Arguments.of("4:30", 50, 50, 3),
        Arguments.of("6 o'clock", 0, 70, 4),
        Arguments.of("7:30", -50, 50, 5),
        Arguments.of("9 o'clock", -70, 0, 6),
        Arguments.of("10:30", -50, -50, 7));
  }

  @ParameterizedTest(name = "{0} -> sector {2}")
  @MethodSource("sectorCenters")
  @DisplayName("each sector center maps to its index, clockwise from 12 o'clock")
  void sectorCentersMapClockwise(
      final String label, final double dx, final double dy, final int expected) {
    assertEquals(expected, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  static Stream<Arguments> sectorBoundaries() {
    // Sector 0 spans -22.5 deg to +22.5 deg around 12 o'clock; points just inside each side of
    // the boundary at radius 70.
    final double radius = 70;
    final double justInside0 = Math.toRadians(-90 + 22.0);
    final double justInside1 = Math.toRadians(-90 + 23.0);
    return Stream.of(
        Arguments.of(
            "just before the 0/1 boundary",
            radius * Math.cos(justInside0),
            radius * Math.sin(justInside0),
            0),
        Arguments.of(
            "just after the 0/1 boundary",
            radius * Math.cos(justInside1),
            radius * Math.sin(justInside1),
            1));
  }

  @ParameterizedTest(name = "{0} -> sector {2}")
  @MethodSource("sectorBoundaries")
  @DisplayName("points on either side of a sector boundary resolve to the adjacent sectors")
  void sectorBoundariesResolveCleanly(
      final String label, final double dx, final double dy, final int expected) {
    assertEquals(expected, DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER));
  }

  @ParameterizedTest(name = "ring position {0}")
  @MethodSource("ringPositions")
  @DisplayName("detection radii are inclusive across the whole ring band")
  void ringBandIsSelectable(final String label, final double dx, final double dy) {
    final int index = DogCommandWheelScreen.sectorIndexAt(dx, dy, SECTORS, INNER, OUTER);
    assertEquals(0, index);
  }

  static Stream<Arguments> ringPositions() {
    return Stream.of(
        Arguments.of("at inner detection radius", 0, -INNER),
        Arguments.of("mid ring", 0, -(INNER + OUTER) / 2),
        Arguments.of("at outer detection radius", 0, -OUTER));
  }
}
