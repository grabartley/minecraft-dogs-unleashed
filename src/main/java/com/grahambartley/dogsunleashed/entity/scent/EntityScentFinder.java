package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Comparator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class EntityScentFinder implements ScentTargetFinder {

  private final ScentRule rule;
  private final Entity tracker;
  private final int range;
  private @Nullable LivingEntity found;
  private ScentShellScan.Status status = ScentShellScan.Status.SEARCHING;

  EntityScentFinder(final ScentRule rule, final Entity tracker, final int range) {
    this.rule = rule;
    this.tracker = tracker;
    this.range = range;
  }

  @Override
  public ScentShellScan.Status advance(final ServerWorld world) {
    if (this.status != ScentShellScan.Status.SEARCHING) {
      return this.status;
    }
    final Vec3d origin = this.tracker.getPos();
    final double maxSquared = (double) this.range * this.range;
    this.found =
        world
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(origin, origin).expand(this.range),
                entity ->
                    entity != this.tracker
                        && entity.isAlive()
                        && this.rule.matchesEntity(entity)
                        && entity.squaredDistanceTo(origin) <= maxSquared)
            .stream()
            .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(origin)))
            .orElse(null);
    this.status =
        this.found == null ? ScentShellScan.Status.EXHAUSTED : ScentShellScan.Status.FOUND;
    return this.status;
  }

  @Override
  public @Nullable Vec3d target() {
    return this.found == null ? null : this.found.getPos();
  }

  @Override
  public boolean isStillThere(final ServerWorld world) {
    return this.found != null
        && this.found.isAlive()
        && !this.found.isRemoved()
        && this.found.getWorld() == world;
  }
}
