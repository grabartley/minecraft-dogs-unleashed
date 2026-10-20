package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModStatueBlocks;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OxidizableDogStatueBlockTest {

  static Stream<Arguments> stages() {
    return Stream.of(
        Arguments.of(ModStatueBlocks.COPPER, Oxidizable.OxidationLevel.UNAFFECTED, true),
        Arguments.of(ModStatueBlocks.EXPOSED_COPPER, Oxidizable.OxidationLevel.EXPOSED, true),
        Arguments.of(ModStatueBlocks.WEATHERED_COPPER, Oxidizable.OxidationLevel.WEATHERED, true),
        Arguments.of(ModStatueBlocks.OXIDIZED_COPPER, Oxidizable.OxidationLevel.OXIDIZED, false));
  }

  @ParameterizedTest(name = "{0} is {1}, random ticks {2}")
  @MethodSource("stages")
  @DisplayName("each copper base reports its stage and only random ticks while it can still age")
  void stageAndTicking(
      final Block block, final Oxidizable.OxidationLevel level, final boolean ticks) {
    Blocks.refreshShapeCache();
    assertEquals(level, ((OxidizableDogStatueBlock) block).getDegradationLevel());
    assertEquals(ticks, block.getDefaultState().hasRandomTicks());
  }
}
