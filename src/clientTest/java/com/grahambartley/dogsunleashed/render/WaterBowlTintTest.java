package com.grahambartley.dogsunleashed.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WaterBowlTintTest {

  static Stream<Arguments> missingPlace() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(null, BlockPos.ORIGIN),
        Arguments.of(mock(BlockRenderView.class), null));
  }

  @ParameterizedTest(name = "world {0}, pos {1} -> plain water blue")
  @MethodSource("missingPlace")
  @DisplayName("without a place in a world the water is vanilla's default water blue")
  void defaultsToPlainWaterBlue(final BlockRenderView world, final BlockPos pos) {
    assertEquals(0x3F76E4, WaterBowlTint.tint(world, pos));
  }
}
