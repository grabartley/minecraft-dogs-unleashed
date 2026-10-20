package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.ModBlockTags;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class BurialGround {

  private BurialGround() {}

  public static boolean isBurialSpot(final ServerWorld world, final BlockPos ground) {
    if (!world.isChunkLoaded(ground)
        || !world.getBlockState(ground).isIn(ModBlockTags.BONE_BURIAL_GROUND)) {
      return false;
    }
    final BlockState above = world.getBlockState(ground.up());
    return (above.isAir() || above.isOf(Blocks.SNOW))
        && world.getBlockState(ground.up(2)).isAir()
        && world.getFluidState(ground.up()).isEmpty()
        && BoneCaches.get(world).at(ground).isEmpty();
  }

  public static Optional<BlockPos> pickSpot(
      final ServerWorld world, final BlockPos origin, final Random random) {
    return BurialSpotSearch.pick(origin, random, ground -> isBurialSpot(world, ground));
  }
}
