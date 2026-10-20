package com.grahambartley.dogsunleashed.portrait;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortraitBoundsTest {

  private static final double EPSILON = 1.0e-9;
  private static final BlockPos POS = new BlockPos(10, 64, -5);

  @ParameterizedTest(name = "{0} facing {1}")
  @CsvSource({
    "SMALL, NORTH, 10.5, 64.5, -4.03125, 1, 1, 0.0625",
    "SMALL, EAST, 10.03125, 64.5, -4.5, 0.0625, 1, 1",
    "WIDE, SOUTH, 11.0, 64.5, -4.96875, 2, 1, 0.0625",
    "WIDE, WEST, 10.96875, 64.5, -4.0, 0.0625, 1, 2",
    "LARGE, NORTH, 10.0, 65.0, -4.03125, 2, 2, 0.0625",
    "LARGE, EAST, 10.03125, 65.0, -5.0, 0.0625, 2, 2"
  })
  @DisplayName("a portrait hugs the wall behind it and grows up and sideways like a painting")
  void boundsHugTheWall(
      final PortraitSize size,
      final Direction facing,
      final double centreX,
      final double centreY,
      final double centreZ,
      final double lengthX,
      final double lengthY,
      final double lengthZ) {
    final Box box = PortraitBounds.of(POS, facing, size);
    assertEquals(centreX, box.getCenter().x, EPSILON);
    assertEquals(centreY, box.getCenter().y, EPSILON);
    assertEquals(centreZ, box.getCenter().z, EPSILON);
    assertEquals(lengthX, box.getLengthX(), EPSILON);
    assertEquals(lengthY, box.getLengthY(), EPSILON);
    assertEquals(lengthZ, box.getLengthZ(), EPSILON);
  }
}
