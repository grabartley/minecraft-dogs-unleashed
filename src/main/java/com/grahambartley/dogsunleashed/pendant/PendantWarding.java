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

  public static boolean isWarded(final MobEntity mob, final LivingEntity target) {
    return !mob.getWorld()
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            mob.getBoundingBox().expand(WARD_RADIUS),
            dog -> wardsAgainst(dog, mob) && (dog == target || dog.isOwner(target)))
        .isEmpty();
  }

  public static void scan(final ServerWorld world) {
    for (final UnleashedDogEntity dog :
        world.getEntitiesByType(
            TypeFilter.instanceOf(UnleashedDogEntity.class), PendantWarding::wearsWard)) {
      for (final MobEntity mob :
          world.getEntitiesByClass(
              MobEntity.class,
              dog.getBoundingBox().expand(WARD_RADIUS),
              mob -> wardsAgainst(dog, mob))) {
        final LivingEntity target = currentTarget(mob);
        if (target != null && (target == dog || dog.isOwner(target))) {
          clearTarget(mob);
        }
      }
    }
  }

  private static boolean wearsWard(final UnleashedDogEntity dog) {
    final PendantType worn = dog.getPendants().worn();
    return worn != null && worn.family() == PendantFamily.WARDING && dog.isAlive();
  }

  private static boolean wardsAgainst(final UnleashedDogEntity dog, final MobEntity mob) {
    return wearsWard(dog)
        && mob.getType().isIn(dog.getPendants().worn().wardedMobs())
        && mob.squaredDistanceTo(dog) <= WARD_RADIUS * WARD_RADIUS;
  }

  private static LivingEntity currentTarget(final MobEntity mob) {
    if (mob.getTarget() != null) {
      return mob.getTarget();
    }
    return mob.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)
        ? mob.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)
        : null;
  }

  private static void clearTarget(final MobEntity mob) {
    mob.setTarget(null);
    if (mob.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET)) {
      mob.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }
  }
}
