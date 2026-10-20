package com.grahambartley.dogsunleashed.entity.scent;

import org.jetbrains.annotations.Nullable;

public enum ScentTargetKind {
  ENTITY,
  BLOCK,
  SUSPICIOUS_BLOCK;

  public static @Nullable ScentTargetKind of(
      final boolean hasEntities, final boolean hasBlocks, final boolean suspiciousBlocks) {
    final int declared = (hasEntities ? 1 : 0) + (hasBlocks ? 1 : 0) + (suspiciousBlocks ? 1 : 0);
    if (declared != 1) {
      return null;
    }
    if (hasEntities) {
      return ENTITY;
    }
    return hasBlocks ? BLOCK : SUSPICIOUS_BLOCK;
  }
}
