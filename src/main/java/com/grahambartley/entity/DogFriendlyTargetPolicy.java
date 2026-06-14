package com.grahambartley.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import org.jetbrains.annotations.Nullable;

public final class DogFriendlyTargetPolicy {
  private DogFriendlyTargetPolicy() {}

  public static final Class<?>[] FRIENDLY_TARGET_TYPES =
      new Class<?>[] {UnleashedDogEntity.class, VillagerEntity.class, IronGolemEntity.class};

  public static boolean isFriendlyTarget(@Nullable Entity entity) {
    return entity != null && isFriendlyClass(entity.getClass());
  }

  public static boolean isFriendlyClass(@Nullable Class<?> entityClass) {
    if (entityClass == null) {
      return false;
    }
    for (Class<?> friendlyType : FRIENDLY_TARGET_TYPES) {
      if (friendlyType.isAssignableFrom(entityClass)) {
        return true;
      }
    }
    return false;
  }
}
