package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogHouseShapeTest {

  private static final double PROBE = 0.03;

  private static VoxelShape probe(
      final double x, final double y, final double z, final Direction facing) {
    double px = x;
    double pz = z;
    for (int turn = 0; turn < quarterTurns(facing); turn++) {
      final double rotatedX = 1 - pz;
      pz = px;
      px = rotatedX;
    }
    return VoxelShapes.cuboid(px - PROBE, y - PROBE, pz - PROBE, px + PROBE, y + PROBE, pz + PROBE);
  }

  private static int quarterTurns(final Direction facing) {
    return switch (facing) {
      case EAST -> 1;
      case SOUTH -> 2;
      case WEST -> 3;
      default -> 0;
    };
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

  @Test
  @DisplayName("the roof leaves headroom, so an occupant is not suffocating in the ceiling")
  void theRoofCellsLeaveHeadroom() {
    for (final DogHousePart part : DogHousePart.values()) {
      if (part.isLower()) {
        continue;
      }
      assertFalse(
          blocks(
              DogHouseShape.collision(part, Direction.NORTH),
              VoxelShapes.cuboid(0.4, 0.0, 0.4, 0.6, 0.3, 0.6)),
          part.asString() + " is roof and must not crush the dog below it");
    }
  }
}
