package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogSleepSpotAnchorTest {

  private static final BlockPos ORIGIN = new BlockPos(10, 70, -30);

  @ParameterizedTest(name = "{0} facing {1}")
  @EnumSource(DogHousePart.class)
  @DisplayName("a dog heads for the house's doorstep whichever cell it was assigned")
  void houseApproachIsTheDoorstep(final DogHousePart part) {
    for (final Direction facing : Direction.Type.HORIZONTAL) {
      final BlockPos cell = ORIGIN.add(DogHouseLayout.offsetFromOrigin(part, facing));
      final BlockState state =
          ModBlocks.DOG_HOUSE
              .getDefaultState()
              .with(DogHouseBlock.FACING, facing)
              .with(DogHouseBlock.PART, part);

      assertEquals(
          DogHouseLayout.doorstep(ORIGIN, facing),
          DogSleepSpotAnchor.approachPoint(worldWith(state), cell),
          facing.asString());
    }
  }

  @Test
  @DisplayName("a dog heads for the middle of a dog bed")
  void bedApproachIsItsCentre() {
    final BlockPos bed = new BlockPos(3, 64, 7);

    assertEquals(
        new Vec3d(3.5, 64.0, 7.5),
        DogSleepSpotAnchor.approachPoint(worldWith(ModBlocks.DOG_BED.getDefaultState()), bed));
  }

  private static BlockView worldWith(final BlockState state) {
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any())).thenReturn(state);
    return world;
  }
}
