package com.grahambartley.dogsunleashed.entity.scent;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

final class BlockScentFinder implements ScentTargetFinder {

  static final int PROBES_PER_TICK = 4096;
  static final int VERTICAL_REACH = 12;

  private final ScentRule rule;
  private final BlockPos origin;
  private final ScentShellScan scan;
  private final BlockPos.Mutable probe = new BlockPos.Mutable();
  private @Nullable BlockPos found;

  BlockScentFinder(final ScentRule rule, final BlockPos origin, final int range) {
    this.rule = rule;
    this.origin = origin.toImmutable();
    this.scan = new ScentShellScan(range, VERTICAL_REACH);
  }

  @Override
  public ScentShellScan.Status advance(final ServerWorld world) {
    final ScentShellScan.Status status =
        this.scan.advance(PROBES_PER_TICK, (dx, dy, dz) -> this.matchDistance(world, dx, dy, dz));
    final ScentShellScan.Offset offset = this.scan.found();
    if (status == ScentShellScan.Status.FOUND && offset != null) {
      this.found = this.origin.add(offset.dx(), offset.dy(), offset.dz());
    }
    return status;
  }

  @Override
  public @Nullable Vec3d target() {
    return this.found == null ? null : Vec3d.ofCenter(this.found);
  }

  @Override
  public boolean isStillThere(final ServerWorld world) {
    return this.found != null
        && world.isChunkLoaded(this.found)
        && this.rule.matchesBlock(world.getBlockState(this.found));
  }

  private long matchDistance(final ServerWorld world, final int dx, final int dy, final int dz) {
    this.probe.set(this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz);
    return !world.isOutOfHeightLimit(this.probe)
            && world.isChunkLoaded(this.probe)
            && this.rule.matchesBlock(world.getBlockState(this.probe))
        ? new ScentShellScan.Offset(dx, dy, dz).squaredLength()
        : ScentShellScan.NO_MATCH;
  }
}
