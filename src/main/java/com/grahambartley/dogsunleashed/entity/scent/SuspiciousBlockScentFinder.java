package com.grahambartley.dogsunleashed.entity.scent;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;

final class SuspiciousBlockScentFinder implements ScentTargetFinder {

  static final int CHUNKS_PER_TICK = 8;
  private static final int CHUNK_SIZE = 16;

  private final BlockPos origin;
  private final int range;
  private final ScentShellScan scan;
  private @Nullable BlockPos found;

  SuspiciousBlockScentFinder(final BlockPos origin, final int range) {
    this.origin = origin.toImmutable();
    this.range = range;
    this.scan = new ScentShellScan(Math.ceilDiv(range, CHUNK_SIZE), 0, CHUNK_SIZE);
  }

  @Override
  public ScentTargetKind kind() {
    return ScentTargetKind.SUSPICIOUS_BLOCK;
  }

  @Override
  public ScentShellScan.Status advance(final ServerWorld world) {
    return this.scan.advance(CHUNKS_PER_TICK, (dx, dy, dz) -> this.searchChunk(world, dx, dz));
  }

  @Override
  public @Nullable Vec3d target() {
    return this.found == null ? null : Vec3d.ofCenter(this.found);
  }

  @Override
  public boolean isStillThere(final ServerWorld world) {
    return this.found != null
        && world.isChunkLoaded(this.found)
        && world.getBlockEntity(this.found) instanceof BrushableBlockEntity;
  }

  private long searchChunk(final ServerWorld world, final int dx, final int dz) {
    final WorldChunk chunk =
        world
            .getChunkManager()
            .getWorldChunk(
                ChunkSectionPos.getSectionCoord(this.origin.getX()) + dx,
                ChunkSectionPos.getSectionCoord(this.origin.getZ()) + dz);
    if (chunk == null) {
      return ScentShellScan.NO_MATCH;
    }
    final List<BlockPos> brushable =
        chunk.getBlockEntities().values().stream()
            .filter(BrushableBlockEntity.class::isInstance)
            .map(BlockEntity::getPos)
            .toList();
    final Optional<BlockPos> nearest =
        SuspiciousScanRules.nearestWithin(this.origin, this.range, brushable);
    if (nearest.isEmpty()) {
      return ScentShellScan.NO_MATCH;
    }
    if (SuspiciousScanRules.isCloser(this.origin, nearest.get(), this.found)) {
      this.found = nearest.get().toImmutable();
    }
    return SuspiciousScanRules.squaredDistance(this.origin, nearest.get());
  }
}
