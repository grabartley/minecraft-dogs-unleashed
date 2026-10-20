package com.grahambartley.dogsunleashed.entity.scent;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class ScentTargetFinders {

  static final int STRUCTURE_SEARCH_RADIUS = 3;

  private ScentTargetFinders() {}

  public static ScentTargetFinder create(
      final ScentRule rule,
      final Entity tracker,
      final int range,
      final ScentLandmarkCache landmarks) {
    return switch (rule.kind()) {
      case ENTITY -> new EntityScentFinder(rule, tracker, range);
      case BLOCK -> new BlockScentFinder(rule, tracker.getBlockPos(), range);
      case SUSPICIOUS_BLOCK -> new SuspiciousBlockScentFinder(tracker.getBlockPos(), range);
      case LANDMARK ->
          new LandmarkScentFinder(
              tracker,
              landmarks,
              world -> locate(world, rule.structures().orElseThrow(), tracker.getBlockPos()));
    };
  }

  public static ScentTargetFinder known(final Entity tracker, final ScentLandmarkCache landmarks) {
    return new LandmarkScentFinder(tracker, landmarks, world -> Optional.empty());
  }

  private static Optional<ScentLandmark> locate(
      final ServerWorld world, final TagKey<Structure> structures, final BlockPos origin) {
    final BlockPos found =
        world.locateStructure(structures, origin, STRUCTURE_SEARCH_RADIUS, false);
    return Optional.ofNullable(found).map(pos -> new ScentLandmark(pos.getX(), pos.getZ()));
  }
}
