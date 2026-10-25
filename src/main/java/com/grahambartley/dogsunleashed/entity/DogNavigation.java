package com.grahambartley.dogsunleashed.entity;

import java.util.Set;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class DogNavigation extends MobNavigation {

  private final double nodeBudgetRange;
  private Set<BlockPos> missedTargets = Set.of();
  private long missedAt;

  public DogNavigation(final MobEntity dog, final World world) {
    super(dog, world);
    this.nodeBudgetRange = dog.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
  }

  @Override
  protected @Nullable Path findPathTo(
      final Set<BlockPos> positions,
      final int range,
      final boolean useHeadPos,
      final int distance) {
    final long now = this.world.getTime();
    if (this.isIdle()
        && positions.equals(this.missedTargets)
        && !DogNavigationRules.mayRetryUnreachable(this.missedAt, now)) {
      return null;
    }
    final double searchRange =
        DogNavigationRules.searchRange(
            this.distanceToNearest(positions),
            this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE));
    this.setRangeMultiplier(DogNavigationRules.nodeBudgetScale(searchRange, this.nodeBudgetRange));
    final Path path =
        this.findPathToAny(positions, range, useHeadPos, distance, (float) searchRange);
    if (path != null && !path.reachesTarget()) {
      this.missedTargets = Set.copyOf(positions);
      this.missedAt = now;
    } else {
      this.missedTargets = Set.of();
    }
    return path;
  }

  private double distanceToNearest(final Set<BlockPos> positions) {
    final BlockPos from = this.entity.getBlockPos();
    return positions.stream()
        .mapToDouble(pos -> Math.sqrt(from.getSquaredDistance(pos)))
        .min()
        .orElse(0.0);
  }
}
