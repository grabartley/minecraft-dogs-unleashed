package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogHouseShapeTest {

  private static final double PROBE = 0.03;

  private static VoxelShape probe(
      final double right, final double y, final double back, final Direction facing) {
    final Direction rightward = facing.rotateYClockwise();
    final Direction backward = facing.getOpposite();
    final double x =
        0.5 + (right - 0.5) * rightward.getOffsetX() + (back - 0.5) * backward.getOffsetX();
    final double z =
        0.5 + (right - 0.5) * rightward.getOffsetZ() + (back - 0.5) * backward.getOffsetZ();
    return VoxelShapes.cuboid(x - PROBE, y - PROBE, z - PROBE, x + PROBE, y + PROBE, z + PROBE);
  }

  private static boolean blocks(final VoxelShape shell, final VoxelShape probe) {
    return VoxelShapes.matchesAnywhere(shell, probe, BooleanBiFunction.AND);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the doorway is a real opening across both front cells, not a painted-on one")
  void theDoorwayIsOpenAcrossBothFrontCells(final Direction facing) {
    assertFalse(
        blocks(
            DogHouseShape.collision(DogHousePart.FRONT_LEFT_LOWER, facing),
            probe(0.8, 0.4, 0.05, facing)),
        "the left half of the doorway should be open");
    assertFalse(
        blocks(
            DogHouseShape.collision(DogHousePart.FRONT_RIGHT_LOWER, facing),
            probe(0.2, 0.4, 0.05, facing)),
        "the right half of the doorway should be open");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the doorway is framed by posts rather than running the full width")
  void theDoorwayIsFramedByPosts(final Direction facing) {
    assertTrue(
        blocks(
            DogHouseShape.collision(DogHousePart.FRONT_LEFT_LOWER, facing),
            probe(0.2, 0.4, 0.05, facing)),
        "the left door post should be solid");
    assertTrue(
        blocks(
            DogHouseShape.collision(DogHousePart.FRONT_RIGHT_LOWER, facing),
            probe(0.8, 0.4, 0.05, facing)),
        "the right door post should be solid");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the wall opposite the doorway stays solid, so the house is not a tunnel")
  void theBackStaysWalled(final Direction facing) {
    assertTrue(
        blocks(
            DogHouseShape.collision(DogHousePart.BACK_LEFT_LOWER, facing),
            probe(0.5, 0.4, 0.97, facing)),
        "the back wall should be solid");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the interior is hollow, so an occupant lies inside rather than inside a wall")
  void theInteriorIsHollow(final Direction facing) {
    for (final DogHousePart part :
        new DogHousePart[] {
          DogHousePart.FRONT_LEFT_LOWER, DogHousePart.FRONT_RIGHT_LOWER,
          DogHousePart.BACK_LEFT_LOWER, DogHousePart.BACK_RIGHT_LOWER
        }) {
      assertFalse(
          blocks(DogHouseShape.collision(part, facing), probe(0.5, 0.5, 0.5, facing)),
          part.asString() + " should be hollow at its middle");
    }
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the floor is solid, so an occupant does not fall through the house")
  void theFloorIsSolid(final Direction facing) {
    assertTrue(
        blocks(
            DogHouseShape.collision(DogHousePart.BACK_RIGHT_LOWER, facing),
            probe(0.5, 0.03, 0.5, facing)),
        "the house needs a floor to lie on");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the interior keeps its headroom up to the ceiling")
  void theInteriorKeepsItsHeadroom(final Direction facing) {
    assertFalse(
        blocks(
            DogHouseShape.collision(DogHousePart.BACK_LEFT_LOWER, facing),
            probe(0.5, 0.95, 0.5, facing)),
        "the lower cell should be open right up to its top");
    assertFalse(
        blocks(
            DogHouseShape.collision(DogHousePart.BACK_LEFT_UPPER, facing),
            probe(0.5, 0.05, 0.5, facing)),
        "the ceiling should start above the walls, not at the cell boundary");
  }

  static Stream<Arguments> roofCellsByFacing() {
    return Arrays.stream(DogHousePart.values())
        .filter(part -> part.up() == 1)
        .flatMap(
            part ->
                Stream.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
                    .map(facing -> Arguments.of(part, facing)));
  }

  @ParameterizedTest(name = "{0} facing {1}")
  @MethodSource("roofCellsByFacing")
  @DisplayName("every roof cell is solid, so a player can stand on the house")
  void everyRoofCellIsSolid(final DogHousePart part, final Direction facing) {
    assertTrue(blocks(DogHouseShape.collision(part, facing), probe(0.5, 0.25, 0.5, facing)));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the roof rises to a ridge along the middle, matching the gable on the model")
  void theRoofRisesToARidge(final Direction facing) {
    final VoxelShape upperLeft = DogHouseShape.collision(DogHousePart.FRONT_LEFT_UPPER, facing);
    assertTrue(blocks(upperLeft, probe(0.97, 0.85, 0.5, facing)), "the ridge should be solid");
    assertFalse(
        blocks(upperLeft, probe(0.1, 0.85, 0.5, facing)), "the eave should be below ridge height");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the chimney is solid on the left side of the roof")
  void theChimneyIsSolid(final Direction facing) {
    assertTrue(
        blocks(
            DogHouseShape.collision(DogHousePart.FRONT_LEFT_UPPER, facing),
            probe(0.55, 0.95, 0.9, facing)),
        "the chimney should block above the roof line");
  }
}
