package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.FoodBowlBlockEntity;
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
class FoodBowlBlockTest {

  private static BlockState stateOf(final BowlFill fill) {
    return ModBlocks.FOOD_BOWL.getDefaultState().with(FoodBowlBlock.FILL, fill);
  }

  @ParameterizedTest(name = "a {0} bowl is a low bowl inside its cell")
  @EnumSource(BowlFill.class)
  @DisplayName("the hitbox is a bowl about a third of a block high, whatever it holds")
  void hitboxIsALowBowl(final BowlFill fill) {
    final VoxelShape shape =
        stateOf(fill).getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
    assertEquals(0.0, shape.getMin(Direction.Axis.Y), 1.0e-9);
    assertEquals(5.0 / 16.0, shape.getMax(Direction.Axis.Y), 1.0e-9);
    assertEquals(2.0 / 16.0, shape.getMin(Direction.Axis.X), 1.0e-9);
    assertEquals(14.0 / 16.0, shape.getMax(Direction.Axis.Z), 1.0e-9);
  }

  @ParameterizedTest(name = "a {0} bowl feeds a comparator")
  @EnumSource(BowlFill.class)
  @DisplayName("comparators can read how full the bowl is")
  void hasComparatorOutput(final BowlFill fill) {
    assertTrue(stateOf(fill).hasComparatorOutput());
  }

  @ParameterizedTest(name = "a {0} bowl keeps a food store")
  @EnumSource(BowlFill.class)
  @DisplayName("every fill level is backed by a food bowl block entity that hoppers can fill")
  void createsAFoodStore(final BowlFill fill) {
    assertInstanceOf(
        FoodBowlBlockEntity.class,
        ((FoodBowlBlock) ModBlocks.FOOD_BOWL).createBlockEntity(BlockPos.ORIGIN, stateOf(fill)));
  }
}
