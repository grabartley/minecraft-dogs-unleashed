package com.grahambartley.dogsunleashed.entity.goal;

import com.grahambartley.dogsunleashed.entity.goal.PlayChaseRules.Spot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

final class StandingGround {

  private static final int VERTICAL_RANGE = 3;

  private StandingGround() {}

  static double yAt(final World world, final Spot spot, final double fromY) {
    final BlockPos level = BlockPos.ofFloored(spot.x(), fromY, spot.z());
    for (int step = 0; step <= 2 * VERTICAL_RANGE; step++) {
      final BlockPos feet = level.up(PlayChaseRules.searchOffset(step));
      if (isClear(world, feet)
          && isClear(world, feet.up())
          && !world.getBlockState(feet.down()).getCollisionShape(world, feet.down()).isEmpty()) {
        return feet.getY();
      }
    }
    return fromY;
  }

  private static boolean isClear(final World world, final BlockPos pos) {
    return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
  }
}
