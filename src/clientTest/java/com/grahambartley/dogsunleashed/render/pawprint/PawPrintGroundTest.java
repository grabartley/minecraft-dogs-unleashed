package com.grahambartley.dogsunleashed.render.pawprint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.SlabType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PawPrintGroundTest {

  private static final BlockPos BELOW = new BlockPos(4, 63, 9);
  private static final BlockPos FEET = BELOW.up();

  @ParameterizedTest(name = "y {0} on a surface at {1} that is full: {2} -> {3}")
  @CsvSource({
    "64.0, 64.0, true, true",
    "64.0005, 64.0, true, true",
    "64.5, 64.0, true, false",
    "64.0, 64.5, true, false",
    "64.0, 64.0, false, false"
  })
  @DisplayName("a print rests only on a full top face at the dog's feet")
  void restsOnlyOnAFullTopFaceAtTheFeet(
      final double y, final double surfaceY, final boolean fullTop, final boolean rests) {
    assertEquals(rests, PawPrintGround.restsOnSurface(y, surfaceY, fullTop));
  }

  @ParameterizedTest(name = "standing on {0} at y {2} with {1} at the feet: {3}")
  @CsvSource({
    "STONE, AIR, 64.0, true",
    "GRASS_BLOCK, AIR, 64.0, true",
    "MUD, AIR, 63.875, true",
    "BOTTOM_SLAB, AIR, 63.5, true",
    "STONE, WATER, 64.0, false",
    "STONE, AIR, 64.5, false",
    "AIR, AIR, 64.0, false",
    "OAK_FENCE, AIR, 65.5, false",
    "LILY_PAD, AIR, 64.09375, false"
  })
  @DisplayName(
      "prints land on a flat full top the dog stands on, never on water, air or narrow tops")
  void landsOnFlatFullTopsOnly(
      final String below, final String feet, final double y, final boolean holds) {
    final BlockView world = worldWith(state(below), state(feet));

    assertEquals(
        holds, PawPrintGround.holdsPrint(world, BELOW.getX() + 0.5, y, BELOW.getZ() + 0.5));
  }

  private static BlockView worldWith(final BlockState below, final BlockState feet) {
    final Map<BlockPos, BlockState> blocks = Map.of(BELOW, below, FEET, feet);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any()))
        .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.getDefaultState()));
    when(world.getFluidState(any()))
        .thenAnswer(call -> world.getBlockState(call.getArgument(0)).getFluidState());
    return world;
  }

  private static BlockState state(final String name) {
    return switch (name) {
      case "BOTTOM_SLAB" ->
          Blocks.STONE_SLAB.getDefaultState().with(Properties.SLAB_TYPE, SlabType.BOTTOM);
      case "WATER" -> Blocks.WATER.getDefaultState();
      case "OAK_FENCE" -> Blocks.OAK_FENCE.getDefaultState();
      case "LILY_PAD" -> Blocks.LILY_PAD.getDefaultState();
      case "GRASS_BLOCK" -> Blocks.GRASS_BLOCK.getDefaultState();
      case "MUD" -> Blocks.MUD.getDefaultState();
      case "STONE" -> Blocks.STONE.getDefaultState();
      default -> Blocks.AIR.getDefaultState();
    };
  }
}
