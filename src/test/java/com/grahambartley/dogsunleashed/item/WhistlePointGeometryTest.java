package com.grahambartley.dogsunleashed.item;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WhistlePointGeometryTest {

  private static final Box UNIT = new Box(0, 0, 0, 1, 1, 1);
  private static final double EPSILON = 1.0e-9;

  @ParameterizedTest(name = "eye ({0}, {1}, {2}) -> ({3}, {4}, {5})")
  @CsvSource({
    "5, 0.5, 0.5, 1, 0.5, 0.5",
    "-3, 2, -3, 0, 1, 0",
    "0.25, 0.75, 0.5, 0.25, 0.75, 0.5",
    "0.5, -10, 9, 0.5, 0, 1"
  })
  @DisplayName("the closest point on a box clamps the eye to each side")
  void closestPointClampsToTheBox(
      final double x,
      final double y,
      final double z,
      final double ex,
      final double ey,
      final double ez) {
    final Vec3d point = WhistlePointGeometry.closestPoint(UNIT, new Vec3d(x, y, z));
    assertEquals(ex, point.x, EPSILON);
    assertEquals(ey, point.y, EPSILON);
    assertEquals(ez, point.z, EPSILON);
  }

  @ParameterizedTest(name = "{0} -> ({1}, {2}, {3})")
  @CsvSource({
    "UP, 0.5, 0.99, 0.5",
    "DOWN, 0.5, 0.01, 0.5",
    "EAST, 0.99, 0.5, 0.5",
    "WEST, 0.01, 0.5, 0.5",
    "SOUTH, 0.5, 0.5, 0.99",
    "NORTH, 0.5, 0.5, 0.01"
  })
  @DisplayName("a face centre sits just inside the middle of that face")
  void faceCentreSitsJustInside(
      final Direction face, final double ex, final double ey, final double ez) {
    final Vec3d centre = WhistlePointGeometry.faceCentre(UNIT, face);
    assertEquals(ex, centre.x, EPSILON);
    assertEquals(ey, centre.y, EPSILON);
    assertEquals(ez, centre.z, EPSILON);
  }

  @ParameterizedTest(name = "slab top face -> y {0}")
  @CsvSource({"0.49"})
  @DisplayName("a face centre follows the block's own shape, such as a slab")
  void faceCentreFollowsTheShape(final double expectedY) {
    assertEquals(
        expectedY,
        WhistlePointGeometry.faceCentre(new Box(0, 0, 0, 1, 0.5, 1), Direction.UP).y,
        EPSILON);
  }

  @ParameterizedTest(name = "aimed {0}")
  @CsvSource({
    "NORTH, NORTH DOWN UP SOUTH WEST EAST",
    "DOWN, DOWN UP NORTH SOUTH WEST EAST",
    "EAST, EAST DOWN UP NORTH SOUTH WEST"
  })
  @DisplayName("after the aimed face the rest are tried down, up, north, south, west, east")
  void triesTheOtherFacesInVanillaOrder(final Direction aimed, final String expected) {
    assertEquals(
        Arrays.stream(expected.split(" ")).map(Direction::valueOf).toList(),
        WhistlePointGeometry.facesToTry(aimed));
  }

  @ParameterizedTest(name = "aimed {0}")
  @EnumSource(Direction.class)
  @DisplayName("the aimed face is tried first, then every other face once")
  void triesTheAimedFaceFirst(final Direction aimed) {
    final List<Direction> faces = WhistlePointGeometry.facesToTry(aimed);
    assertEquals(aimed, faces.get(0));
    assertEquals(Direction.values().length, faces.size());
    assertEquals(EnumSet.allOf(Direction.class), EnumSet.copyOf(faces));
  }
}
