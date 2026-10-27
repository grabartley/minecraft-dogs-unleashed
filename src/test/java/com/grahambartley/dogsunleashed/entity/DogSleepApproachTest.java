package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.ModBlocks;
import com.grahambartley.dogsunleashed.block.DogHouseBlock;
import com.grahambartley.dogsunleashed.block.DogHouseLayout;
import com.grahambartley.dogsunleashed.block.DogHousePart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogSleepApproachTest {

  private static final Vec3d POINT = new Vec3d(10.5, 64.0, -3.5);

  @ParameterizedTest(name = "dog at ({0}, {1}, {2}) arrived: {3}")
  @CsvSource({
    "10.5, 64.0, -3.5, true",
    "12.5, 64.0, -3.5, true",
    "10.5, 64.0, -5.5, true",
    "11.9, 64.0, -2.1, true",
    "12.6, 64.0, -3.5, false",
    "12.0, 64.0, -1.9, false",
    "10.5, 66.0, -3.5, true",
    "10.5, 66.1, -3.5, false",
    "10.5, 61.9, -3.5, false"
  })
  @DisplayName("a dog has arrived within two blocks of the approach point, sideways and up or down")
  void arrivesWithinTwoBlocks(
      final double x, final double y, final double z, final boolean arrived) {
    assertEquals(arrived, DogSleepApproach.hasArrived(new Vec3d(x, y, z), POINT));
  }

  private static final BlockPos HOUSE_ORIGIN = new BlockPos(20, 64, 20);

  @ParameterizedTest(name = "dog at offset ({0}, {1}) from the doorstep arrived: {2}")
  @CsvSource({
    "0.0, 0.0, true",
    "1.5, 0.0, true",
    "-2.5, 0.0, true",
    "-4.5, 0.0, false",
    "0.0, 3.0, false"
  })
  @DisplayName("a dog reaches a north-facing house at its doorstep or right by its origin cell")
  void reachesTheHouseAtTheDoorstep(
      final double alongFacing, final double sideways, final boolean arrived) {
    final Vec3d doorstep = DogHouseLayout.doorstep(HOUSE_ORIGIN, Direction.NORTH);
    final Vec3d dog = doorstep.add(sideways, 0.0, -alongFacing);
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any()))
        .thenReturn(
            ModBlocks.DOG_HOUSE
                .getDefaultState()
                .with(DogHouseBlock.FACING, Direction.NORTH)
                .with(DogHouseBlock.PART, DogHousePart.ORIGIN));

    assertEquals(arrived, DogSleepApproach.hasArrived(world, dog, HOUSE_ORIGIN));
  }
}
