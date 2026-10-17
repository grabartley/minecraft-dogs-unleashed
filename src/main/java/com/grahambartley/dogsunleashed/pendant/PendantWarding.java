package com.grahambartley.dogsunleashed.pendant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantFamily;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.TypeFilter;

public final class PendantWarding {

  public static final double WARD_RADIUS = 16.0;
  public static final int SCAN_INTERVAL_TICKS = 10;

  private PendantWarding() {}

  public static void scan(final ServerWorld world) {
    for (final UnleashedDogEntity dog :
        world.getEntitiesByType(
            TypeFilter.instanceOf(UnleashedDogEntity.class), PendantWarding::wearsWard)) {
      final PendantType ward = dog.getPendants().worn();
      final LivingEntity owner = dog.getOwner();
      for (final MobEntity mob :
          world.getEntitiesByClass(
              MobEntity.class,
              dog.getBoundingBox().expand(WARD_RADIUS),
              mob -> mob.getType().isIn(ward.wardedMobs()))) {
        if (isTargeting(mob, dog) || (owner != null && isTargeting(mob, owner))) {
          clearTarget(mob);
        }
      }
    }
  }

  private static boolean wearsWard(final UnleashedDogEntity dog) {
    final PendantType worn = dog.getPendants().worn();
    return worn != null && worn.family() == PendantFamily.WARDING && dog.isAlive();
  }

  private static boolean isTargeting(final MobEntity mob, final LivingEntity entity) {
    return mob.getTarget() == entity
        || (mob.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)
            && mob.getBrain()
                .getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET)
                .filter(target -> target == entity)
                .isPresent());
  }

  private static void clearTarget(final MobEntity mob) {
    mob.setTarget(null);
    if (mob.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
      mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }
  }
}
