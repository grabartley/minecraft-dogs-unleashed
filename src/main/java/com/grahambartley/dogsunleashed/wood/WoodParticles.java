package com.grahambartley.dogsunleashed.wood;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public final class WoodParticles {

  private WoodParticles() {}

  public static BlockState appearanceOf(
      final BlockState state, final BlockView world, final BlockPos pos) {
    return state.getBlock() instanceof WoodenBlock wooden
        ? WoodPlanks.planksOf(wooden.woodAt(world, sourceOf(state, world, pos), state))
            .getDefaultState()
        : state;
  }

  private static BlockPos sourceOf(
      final BlockState state, final BlockView world, final BlockPos pos) {
    return world.getBlockState(pos) != state && world.getBlockState(pos.down()) == state
        ? pos.down()
        : pos;
  }
}
