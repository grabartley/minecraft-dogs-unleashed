package com.grahambartley.dogsunleashed.entity.scent;

import org.jetbrains.annotations.Nullable;

public enum ScentTargetKind {
  ENTITY,
  BLOCK,
  SUSPICIOUS_BLOCK,
  LANDMARK;

  public static @Nullable ScentTargetKind of(
      final boolean hasEntities,
      final boolean hasBlocks,
      final boolean suspiciousBlocks,
      final boolean hasStructures) {
    final int declared =
        (hasEntities ? 1 : 0)
            + (hasBlocks ? 1 : 0)
            + (suspiciousBlocks ? 1 : 0)
            + (hasStructures ? 1 : 0);
    if (declared != 1) {
      return null;
    }
    if (hasEntities) {
      return ENTITY;
    }
    if (hasBlocks) {
      return BLOCK;
    }
    return suspiciousBlocks ? SUSPICIOUS_BLOCK : LANDMARK;
  }
}
