package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DogHouseLayoutTest {

  private static final BlockPos ORIGIN = new BlockPos(10, 70, -30);

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the house is eight distinct cells whichever way it is turned")
  void theHouseIsEightDistinctCells(final Direction facing) {
    final Set<BlockPos> cells = new HashSet<>(DogHouseLayout.cellsOf(ORIGIN, facing));

    assertEquals(
        DogHousePart.values().length,
        cells.size(),
        "two parts landing in one cell would leave a hole in the house");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the footprint is two cells on a side and two levels tall")
  void theFootprintIsTwoByTwoByTwo(final Direction facing) {
    final List<BlockPos> cells = DogHouseLayout.cellsOf(ORIGIN, facing);
    final int width = span(cells, BlockPos::getX);
    final int height = span(cells, BlockPos::getY);
    final int depth = span(cells, BlockPos::getZ);

    assertEquals(2, width, "width");
    assertEquals(2, height, "height");
    assertEquals(2, depth, "depth");
  }

  /** Every cell has to be able to find the block entity, or half the house does nothing on use. */
  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("every part resolves back to the cell it was placed from")
  void everyPartResolvesBackToTheOrigin(final Direction facing) {
    for (final DogHousePart part : DogHousePart.values()) {
      final BlockPos cell = ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, facing));

      assertEquals(ORIGIN, DogHouseLayout.originOf(part, facing, cell), part.asString());
    }
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("an occupant lies at the middle of the footprint, not the middle of one cell")
  void theOccupantAnchorIsTheMiddleOfTheFootprint(final Direction facing) {
    final Vec3d anchor = DogHouseLayout.occupantAnchor(ORIGIN, facing);
    final List<BlockPos> lower =
        DogHouseLayout.cellsOf(ORIGIN, facing).stream()
            .filter(c -> c.getY() == ORIGIN.getY())
            .toList();

    final double midX = (minOf(lower, BlockPos::getX) + maxOf(lower, BlockPos::getX) + 1) / 2.0;
    final double midZ = (minOf(lower, BlockPos::getZ) + maxOf(lower, BlockPos::getZ) + 1) / 2.0;
    assertEquals(midX, anchor.x, 1.0e-9, "x");
    assertEquals(midZ, anchor.z, 1.0e-9, "z");
    assertEquals(ORIGIN.getY(), anchor.y, 1.0e-9, "an occupant lies on the floor");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the doorway cells sit on the side the house faces")
  void theDoorwayCellsSitOnTheFacingSide(final Direction facing) {
    final BlockPos frontLeft =
        ORIGIN.add(DogHouseLayout.offsetFromOrigin(DogHousePart.FRONT_LEFT_LOWER, facing));
    final BlockPos backLeft =
        ORIGIN.add(DogHouseLayout.offsetFromOrigin(DogHousePart.BACK_LEFT_LOWER, facing));

    final BlockPos towardFacing = frontLeft.offset(facing);
    assertTrue(
        towardFacing.getSquaredDistance(backLeft) > frontLeft.getSquaredDistance(backLeft),
        "stepping out of the doorway should move away from the back of the house");
  }

  private static int span(
      final List<BlockPos> cells, final java.util.function.ToIntFunction<BlockPos> axis) {
    return maxOf(cells, axis) - minOf(cells, axis) + 1;
  }

  private static int minOf(
      final List<BlockPos> cells, final java.util.function.ToIntFunction<BlockPos> axis) {
    return cells.stream().mapToInt(axis).min().orElseThrow();
  }

  private static int maxOf(
      final List<BlockPos> cells, final java.util.function.ToIntFunction<BlockPos> axis) {
    return cells.stream().mapToInt(axis).max().orElseThrow();
  }
}
