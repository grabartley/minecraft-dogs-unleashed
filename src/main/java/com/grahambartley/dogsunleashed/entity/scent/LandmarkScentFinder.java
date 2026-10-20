package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

final class LandmarkScentFinder implements ScentTargetFinder {

  private final Entity tracker;
  private final ScentLandmarkCache cache;
  private final Function<ServerWorld, Optional<ScentLandmark>> lookup;
  private ScentShellScan.Status status = ScentShellScan.Status.SEARCHING;

  LandmarkScentFinder(
      final Entity tracker,
      final ScentLandmarkCache cache,
      final Function<ServerWorld, Optional<ScentLandmark>> lookup) {
    this.tracker = tracker;
    this.cache = cache;
    this.lookup = lookup;
  }

  @Override
  public ScentTargetKind kind() {
    return ScentTargetKind.LANDMARK;
  }

  @Override
  public ScentShellScan.Status advance(final ServerWorld world) {
    if (this.status == ScentShellScan.Status.SEARCHING) {
      this.status =
          this.cache.resolve(() -> this.lookup.apply(world)).isPresent()
              ? ScentShellScan.Status.FOUND
              : ScentShellScan.Status.EXHAUSTED;
    }
    return this.status;
  }

  @Override
  public @Nullable Vec3d target() {
    return this.cache
        .known()
        .map(landmark -> new Vec3d(landmark.x() + 0.5, this.surfaceY(landmark), landmark.z() + 0.5))
        .orElse(null);
  }

  @Override
  public boolean isStillThere(final ServerWorld world) {
    return this.cache.known().isPresent();
  }

  private double surfaceY(final ScentLandmark landmark) {
    final BlockPos column = new BlockPos(landmark.x(), 0, landmark.z());
    return this.tracker.getWorld().isChunkLoaded(column)
        ? this.tracker
            .getWorld()
            .getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, landmark.x(), landmark.z())
        : this.tracker.getY();
  }
}
