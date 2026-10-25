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
  private long wideSearchMissedAt = Long.MIN_VALUE;

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
    final double followRange = this.entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
    final BlockPos from = useHeadPos ? this.entity.getBlockPos().up() : this.entity.getBlockPos();
    final double searchRange =
        DogNavigationRules.searchRange(
            DogNavigationRules.nearestDistance(from, positions),
            followRange,
            DogNavigationRules.recentlyMissed(this.wideSearchMissedAt, now));
    this.setRangeMultiplier(DogNavigationRules.nodeBudgetScale(searchRange, this.nodeBudgetRange));
    final Path previous = this.currentPath;
    final Path path =
        this.findPathToAny(positions, range, useHeadPos, distance, (float) searchRange);
    this.resetRangeMultiplier();
    if (path != null
        && path != previous
        && DogNavigationRules.wideSearchMissed(searchRange, followRange, path.reachesTarget())) {
      this.wideSearchMissedAt = now;
    }
    return path;
  }
}
