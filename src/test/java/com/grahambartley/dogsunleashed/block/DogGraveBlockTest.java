package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGraveBlockTest {

  private static BlockState state(final Direction facing, final DoubleBlockHalf half) {
    return ModBlocks.DOG_GRAVE
        .getDefaultState()
        .with(DogGraveBlock.FACING, facing)
        .with(DogGraveBlock.HALF, half);
  }

  private static VoxelShape shapeOf(final Direction facing, final DoubleBlockHalf half) {
    return state(facing, half).getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
  }

  @ParameterizedTest(name = "facing {0}, the base fills its own cell's height and no more")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the base half's hitbox stays inside its own block cell")
  void baseHitboxStaysInsideItsCell(final Direction facing) {
    final VoxelShape shape = shapeOf(facing, DoubleBlockHalf.LOWER);
    assertEquals(0.0, shape.getMin(Direction.Axis.Y), 1.0e-9);
    assertEquals(1.0, shape.getMax(Direction.Axis.Y), 1.0e-9, "a shape past 1.0 cannot be raycast");
  }

  @ParameterizedTest(name = "facing {0}, the upper half carries the top of the headstone")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the upper half's hitbox covers the headstone's top half block")
  void upperHitboxCoversTheHeadstoneTop(final Direction facing) {
    final VoxelShape shape = shapeOf(facing, DoubleBlockHalf.UPPER);
    assertEquals(0.0, shape.getMin(Direction.Axis.Y), 1.0e-9);
    assertEquals(0.5, shape.getMax(Direction.Axis.Y), 1.0e-9);
  }

  @ParameterizedTest(name = "facing {0}, both halves share one footprint")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the two halves stack into one continuous headstone")
  void halvesShareOneFootprint(final Direction facing) {
    final VoxelShape lower = shapeOf(facing, DoubleBlockHalf.LOWER);
    final VoxelShape upper = shapeOf(facing, DoubleBlockHalf.UPPER);
    for (final Direction.Axis axis : new Direction.Axis[] {Direction.Axis.X, Direction.Axis.Z}) {
      assertEquals(lower.getMin(axis), upper.getMin(axis), 1.0e-9, "min " + axis);
      assertEquals(lower.getMax(axis), upper.getMax(axis), 1.0e-9, "max " + axis);
    }
  }

  static Stream<Arguments> basePosCases() {
    final BlockPos pos = new BlockPos(10, 64, -3);
    return Stream.of(
        Arguments.of(DoubleBlockHalf.LOWER, pos, pos),
        Arguments.of(DoubleBlockHalf.UPPER, pos, pos.down()));
  }

  @ParameterizedTest(name = "{0} resolves to {2}")
  @MethodSource("basePosCases")
  @DisplayName("either half resolves to the cell holding the block entity")
  void basePosResolvesToTheEntityCell(
      final DoubleBlockHalf half, final BlockPos pos, final BlockPos expected) {
    assertEquals(expected, DogGraveBlock.basePosOf(state(Direction.NORTH, half), pos));
  }

  @ParameterizedTest(name = "facing {0} is thinner than it is wide")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the slab stays thin along its facing axis")
  void slabStaysThinAlongItsFacingAxis(final Direction facing) {
    final VoxelShape shape = shapeOf(facing, DoubleBlockHalf.LOWER);
    final Direction.Axis thin =
        facing == Direction.EAST || facing == Direction.WEST ? Direction.Axis.X : Direction.Axis.Z;
    assertTrue(
        shape.getMax(thin) - shape.getMin(thin) < 0.5,
        "the headstone should stay a thin slab along " + thin);
  }
}
