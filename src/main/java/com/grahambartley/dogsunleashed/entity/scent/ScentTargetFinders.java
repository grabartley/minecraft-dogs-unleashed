package com.grahambartley.dogsunleashed.entity.scent;

import net.minecraft.entity.Entity;

public final class ScentTargetFinders {

  private ScentTargetFinders() {}

  public static ScentTargetFinder create(
      final ScentRule rule, final Entity tracker, final int range) {
    return switch (rule.kind()) {
      case ENTITY -> new EntityScentFinder(rule, tracker, range);
      case BLOCK -> new BlockScentFinder(rule, tracker.getBlockPos(), range);
      case SUSPICIOUS_BLOCK -> new SuspiciousBlockScentFinder(tracker.getBlockPos(), range);
    };
  }
}
