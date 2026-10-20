package com.grahambartley.dogsunleashed.entity.burial;

import com.grahambartley.dogsunleashed.ModBlockTags;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class BurialGround {

  private static final int VERTICAL_REACH = 4;
  private static final int TRIES = 12;

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
      final ServerWorld world,
      final BlockPos origin,
      final Random random,
      final Predicate<BlockPos> reachable) {
    for (int attempt = 0; attempt < TRIES; attempt++) {
      final BlockPos column =
          origin.add(
              random.nextBetween(-BoneCacheRules.BURY_RADIUS, BoneCacheRules.BURY_RADIUS),
              0,
              random.nextBetween(-BoneCacheRules.BURY_RADIUS, BoneCacheRules.BURY_RADIUS));
      for (int dy = VERTICAL_REACH; dy >= -VERTICAL_REACH; dy--) {
        final BlockPos ground = column.up(dy);
        if (isBurialSpot(world, ground)) {
          if (reachable.test(ground)) {
            return Optional.of(ground);
          }
          break;
        }
      }
    }
    return Optional.empty();
  }
}
