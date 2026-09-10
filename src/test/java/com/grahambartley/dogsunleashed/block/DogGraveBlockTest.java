package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGraveBlockTest {

  private static VoxelShape shapeOf(final Direction facing) {
    final BlockState state =
        ModBlocks.DOG_GRAVE.getDefaultState().with(DogGraveBlock.FACING, facing);
    return state.getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
  }

  @ParameterizedTest(name = "facing {0}, the hitbox spans the full height of its cell")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the hitbox fills its cell top to bottom, so the whole headstone can be aimed at")
  void hitboxFillsItsCellVertically(final Direction facing) {
    final VoxelShape shape = shapeOf(facing);
    assertEquals(0.0, shape.getMin(Direction.Axis.Y), 1.0e-9, "hitbox must start at the ground");
    assertEquals(1.0, shape.getMax(Direction.Axis.Y), 1.0e-9, "hitbox must reach the cell top");
  }

  @ParameterizedTest(name = "facing {0}, the hitbox stays within its cell horizontally")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the hitbox never leaves its own cell, which is the only volume a ray tests")
  void hitboxStaysWithinItsCell(final Direction facing) {
    final VoxelShape shape = shapeOf(facing);
    for (final Direction.Axis axis : Direction.Axis.values()) {
      assertTrue(shape.getMin(axis) >= 0.0, "min " + axis + " leaves the cell");
      assertTrue(shape.getMax(axis) <= 1.0, "max " + axis + " leaves the cell");
    }
  }

  @ParameterizedTest(name = "facing {0} is thinner than it is wide")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the headstone stays a thin slab along its facing axis")
  void slabStaysThinAlongItsFacingAxis(final Direction facing) {
    final VoxelShape shape = shapeOf(facing);
    final Direction.Axis thin =
        facing == Direction.EAST || facing == Direction.WEST ? Direction.Axis.X : Direction.Axis.Z;
    final Direction.Axis wide = thin == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
    assertTrue(
        shape.getMax(thin) - shape.getMin(thin) < shape.getMax(wide) - shape.getMin(wide),
        "the headstone should stay a thin slab along " + thin);
  }
}
