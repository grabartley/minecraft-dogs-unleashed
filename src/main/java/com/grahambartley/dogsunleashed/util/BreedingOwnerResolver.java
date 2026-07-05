package com.grahambartley.dogsunleashed.util;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public final class BreedingOwnerResolver {
  private BreedingOwnerResolver() {}

  public static @Nullable UUID resolveInheritedOwnerUuid(
      final @Nullable UUID primaryOwnerUuid, final @Nullable UUID secondaryOwnerUuid) {
    return primaryOwnerUuid != null ? primaryOwnerUuid : secondaryOwnerUuid;
  }
}
