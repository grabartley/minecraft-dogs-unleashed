package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.pendant.CreeperWardFlee;
import com.grahambartley.dogsunleashed.pendant.OwnerDamageClock;
import com.grahambartley.dogsunleashed.pendant.PendantVitality;
import com.grahambartley.dogsunleashed.pendant.PendantWarding;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class PendantEffectListener {

  private static final OwnerDamageClock DAMAGE_CLOCK = new OwnerDamageClock();

  private PendantEffectListener() {}

  public static void initialize() {
    ServerTickEvents.END_WORLD_TICK.register(PendantEffectListener::onWorldTick);
    ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> CreeperWardFlee.addTo(entity));
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, damageTaken, blocked) -> {
          if (entity instanceof ServerPlayerEntity player && damageTaken > 0.0f) {
            DAMAGE_CLOCK.recordDamage(player.getUuid(), player.getServerWorld().getTime());
          }
        });
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> DAMAGE_CLOCK.forget(handler.getPlayer().getUuid()));
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> DAMAGE_CLOCK.clear());
  }

  private static void onWorldTick(final ServerWorld world) {
    final long tick = world.getTime();
    if (tick % PendantWarding.SCAN_INTERVAL_TICKS == 0) {
      PendantWarding.scan(world);
    }
    if (tick % PendantVitality.CHECK_INTERVAL_TICKS == 0) {
      PendantVitality.apply(world, DAMAGE_CLOCK);
    }
  }
}
