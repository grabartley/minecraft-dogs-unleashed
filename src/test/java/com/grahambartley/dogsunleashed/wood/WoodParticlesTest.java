package com.grahambartley.dogsunleashed.wood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.entity.DogBedBlockEntity;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WoodType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WoodParticlesTest {

  private static final BlockPos POS = new BlockPos(3, 64, -7);

  static Stream<Arguments> woods() {
    return Stream.of(
        Arguments.of(WoodType.OAK, Blocks.OAK_PLANKS),
        Arguments.of(WoodType.SPRUCE, Blocks.SPRUCE_PLANKS),
        Arguments.of(WoodType.MANGROVE, Blocks.MANGROVE_PLANKS),
        Arguments.of(WoodType.CRIMSON, Blocks.CRIMSON_PLANKS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("mining and breaking particles of a wooden block look like its planks")
  void woodenBlockAppearsAsItsPlanks(final WoodType wood, final Block planks) {
    final BlockState bed = ModBlocks.DOG_BED.getDefaultState();
    final BlockView world = worldWithBedAt(POS, bed, wood);

    assertEquals(planks.getDefaultState(), WoodParticles.appearanceOf(bed, world, POS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("woods")
  @DisplayName("particles kicked up by running on top of a wooden block look like its planks")
  void runningParticlesAppearAsThePlanksBelow(final WoodType wood, final Block planks) {
    final BlockState bed = ModBlocks.DOG_BED.getDefaultState();
    final BlockView world = worldWithBedAt(POS.down(), bed, wood);
    when(world.getBlockState(POS)).thenReturn(Blocks.AIR.getDefaultState());

    assertEquals(planks.getDefaultState(), WoodParticles.appearanceOf(bed, world, POS));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("plainBlocks")
  @DisplayName("a block that is not wooden keeps its own particles")
  void plainBlockKeepsItsLook(final Block block) {
    final BlockState state = block.getDefaultState();

    assertSame(state, WoodParticles.appearanceOf(state, mock(BlockView.class), POS));
  }

  static Stream<Block> plainBlocks() {
    return Stream.of(Blocks.STONE, Blocks.OAK_PLANKS, ModBlocks.FOOD_BOWL);
  }

  private static BlockView worldWithBedAt(
      final BlockPos pos, final BlockState bed, final WoodType wood) {
    final DogBedBlockEntity entity = new DogBedBlockEntity(pos, bed);
    entity.setWood(wood);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(pos)).thenReturn(bed);
    when(world.getBlockEntity(pos)).thenReturn(entity);
    return world;
  }
}
