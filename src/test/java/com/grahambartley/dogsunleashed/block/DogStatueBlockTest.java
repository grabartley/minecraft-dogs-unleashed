package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModStatueBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogStatueBlockEntity;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogStatueBlockTest {

  static Stream<Block> bases() {
    return ModStatueBlocks.all().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bases")
  @DisplayName("every base is a half block plinth, so the dog stands on its top at y 0.5")
  void baseIsAHalfBlock(final Block base) {
    assertEquals(
        new Box(0.0, 0.0, 0.0, 1.0, 0.5, 1.0),
        base.getDefaultState()
            .getOutlineShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN)
            .getBoundingBox());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("bases")
  @DisplayName("every base holds a statue block entity")
  void baseCreatesAStatueBlockEntity(final Block base) {
    assertInstanceOf(
        DogStatueBlockEntity.class,
        ((DogStatueBlock) base).createBlockEntity(BlockPos.ORIGIN, base.getDefaultState()));
  }

  @Test
  @DisplayName("a fresh base faces north until it is placed")
  void defaultFacingIsNorth() {
    assertEquals(
        Direction.NORTH, ModStatueBlocks.STONE.getDefaultState().get(DogStatueBlock.FACING));
  }
}
