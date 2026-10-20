package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.expedition.ExpeditionHomecoming;
import com.grahambartley.dogsunleashed.entity.expedition.Expeditions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public final class ExpeditionListener {

  private static final int CHECK_INTERVAL_TICKS = 20;

  private ExpeditionListener() {}

  public static void initialize() {
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
            ExpeditionHomecoming.returnDue(server);
          }
        });
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> {
          if (entity instanceof UnleashedDogEntity dog) {
            discardIfAway(dog, world);
          }
        });
  }

  public static void discardIfAway(final UnleashedDogEntity dog, final ServerWorld world) {
    if (!Expeditions.get(world.getServer()).isAway(dog.getUuid())) {
      return;
    }
    DogsUnleashed.runNextTick(
        () -> {
          if (!dog.isRemoved() && Expeditions.get(world.getServer()).isAway(dog.getUuid())) {
            DogsUnleashed.log.warn("[Expedition] Discarding a stale copy of {}", dog.getUuid());
            dog.discard();
          }
        });
  }
}
