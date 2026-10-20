package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WaterBowlBlockTest {

  private static BlockState stateOf(final int drinks) {
    return ModBlocks.WATER_BOWL.getDefaultState().with(WaterBowlBlock.DRINKS, drinks);
  }

  @ParameterizedTest(name = "a bowl with {0} drinks has water: {1}")
  @CsvSource({"0, false", "1, true", "2, true", "3, true"})
  @DisplayName("a dog can drink from any bowl with at least one drink left")
  void hasWaterWhileDrinksRemain(final int drinks, final boolean expected) {
    assertEquals(expected, WaterBowlBlock.hasWater(stateOf(drinks)));
  }

  @Test
  @DisplayName("a bowl is placed empty, so it has to be filled before dogs drink")
  void placesEmpty() {
    assertFalse(WaterBowlBlock.hasWater(ModBlocks.WATER_BOWL.getDefaultState()));
  }

  @Test
  @DisplayName("other blocks never count as a water bowl, even water itself")
  void otherBlocksHaveNoBowlWater() {
    assertFalse(WaterBowlBlock.hasWater(Blocks.WATER_CAULDRON.getDefaultState()));
  }

  @ParameterizedTest(name = "a bowl with {0} drinks is as tall as the food bowl")
  @CsvSource({"0", "3"})
  @DisplayName("the water bowl shares the food bowl's low hitbox")
  void sharesTheFoodBowlHitbox(final int drinks) {
    assertEquals(
        5.0 / 16.0,
        stateOf(drinks)
            .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
            .getMax(Direction.Axis.Y),
        1.0e-9);
  }
}
