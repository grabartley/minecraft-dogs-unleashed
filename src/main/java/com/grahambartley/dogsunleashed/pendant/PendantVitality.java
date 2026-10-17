package com.grahambartley.dogsunleashed.pendant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class PendantVitality {

  public static final double VITALITY_RADIUS = 8.0;
  public static final int CHECK_INTERVAL_TICKS = 20;

  private PendantVitality() {}

  public static void apply(final ServerWorld world, final OwnerDamageClock damageClock) {
    final long tick = world.getTime();
    for (final ServerPlayerEntity player : world.getPlayers()) {
      if (!player.isAlive()
          || !damageClock.isRecovered(player.getUuid(), tick)
          || !hasVitalityDogNearby(world, player)) {
        continue;
      }
      final StatusEffectInstance current = player.getStatusEffect(StatusEffects.REGENERATION);
      final int duration =
          VitalityCadence.durationToApply(
              current == null ? VitalityCadence.NO_EFFECT : current.getDuration());
      if (duration != VitalityCadence.NO_CHANGE) {
        player.addStatusEffect(
            new StatusEffectInstance(StatusEffects.REGENERATION, duration, 0, false, false, true));
      }
    }
  }

  private static boolean hasVitalityDogNearby(
      final ServerWorld world, final ServerPlayerEntity player) {
    return !world
        .getEntitiesByClass(
            UnleashedDogEntity.class,
            player.getBoundingBox().expand(VITALITY_RADIUS),
            dog ->
                dog.isAlive()
                    && dog.isOwner(player)
                    && dog.getPendants().isWearing(PendantType.VITALITY)
                    && dog.squaredDistanceTo(player) <= VITALITY_RADIUS * VITALITY_RADIUS)
        .isEmpty();
  }
}
