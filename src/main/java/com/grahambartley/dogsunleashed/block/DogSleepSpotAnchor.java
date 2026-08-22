package com.grahambartley.dogsunleashed.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;

/**
 * Where a dog actually lies down in the bed it was assigned.
 *
 * <p>For a one-cell bed that is the middle of its own cell, but the dog house is two cells on a
 * side and its assignment is recorded against a corner cell, so lying at that cell's middle would
 * leave the dog half outside the house.
 */
public final class DogSleepSpotAnchor {

  private static final double CELL_CENTRE = 0.5;

  private DogSleepSpotAnchor() {}

  public static Vec3d of(final BlockView world, final BlockPos bedPos) {
    final BlockState state = world.getBlockState(bedPos);
    if (state.getBlock() instanceof DogHouseBlock) {
      return DogHouseLayout.occupantAnchor(
          DogHouseBlock.originOf(state, bedPos), state.get(DogHouseBlock.FACING));
    }
    return new Vec3d(bedPos.getX() + CELL_CENTRE, bedPos.getY(), bedPos.getZ() + CELL_CENTRE);
  }
}
