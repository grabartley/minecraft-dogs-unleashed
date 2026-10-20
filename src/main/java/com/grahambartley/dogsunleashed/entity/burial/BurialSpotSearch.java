package com.grahambartley.dogsunleashed.entity.burial;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class BurialSpotSearch {

  static final int VERTICAL_REACH = 4;
  static final int TRIES = 16;

  private BurialSpotSearch() {}

  public static Optional<BlockPos> pick(
      final BlockPos origin, final Random random, final Predicate<BlockPos> isSpot) {
    for (int attempt = 0; attempt < TRIES; attempt++) {
      final BlockPos column =
          origin.add(
              random.nextBetween(-BoneCacheRules.BURY_RADIUS, BoneCacheRules.BURY_RADIUS),
              0,
              random.nextBetween(-BoneCacheRules.BURY_RADIUS, BoneCacheRules.BURY_RADIUS));
      for (int dy = VERTICAL_REACH; dy >= -VERTICAL_REACH; dy--) {
        final BlockPos ground = column.up(dy);
        if (isSpot.test(ground)) {
          return Optional.of(ground);
        }
      }
    }
    return Optional.empty();
  }
}
