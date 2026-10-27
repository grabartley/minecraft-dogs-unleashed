package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import java.util.Arrays;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogSleepSpotAnchorTest {

  private static final BlockPos ORIGIN = new BlockPos(10, 70, -30);

  @ParameterizedTest(name = "{0} facing {1}")
  @MethodSource("partsAndFacings")
  @DisplayName("a dog heads for the house's doorstep whichever cell it was assigned")
  void houseApproachIsTheDoorstep(final DogHousePart part, final Direction facing) {
    final BlockPos cell = ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, facing));

    assertEquals(
        DogHouseLayout.doorstepCell(ORIGIN, facing),
        DogSleepSpotAnchor.approachCell(worldWith(houseCell(part, facing)), cell));
  }

  static Stream<Arguments> partsAndFacings() {
    return Arrays.stream(DogHousePart.values())
        .flatMap(
            part -> Direction.Type.HORIZONTAL.stream().map(facing -> Arguments.of(part, facing)));
  }

  static BlockState houseCell(final DogHousePart part, final Direction facing) {
    return ModBlocks.DOG_HOUSE
        .getDefaultState()
        .with(DogHouseBlock.FACING, facing)
        .with(DogHouseBlock.PART, part);
  }

  @Test
  @DisplayName("a dog heads straight for a dog bed")
  void bedApproachIsItsCentre() {
    final BlockPos bed = new BlockPos(3, 64, 7);

    assertEquals(
        bed, DogSleepSpotAnchor.approachCell(worldWith(ModBlocks.DOG_BED.getDefaultState()), bed));
  }

  static BlockView worldWith(final BlockState state) {
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any())).thenReturn(state);
    return world;
  }
}
