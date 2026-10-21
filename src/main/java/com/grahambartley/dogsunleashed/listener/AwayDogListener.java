package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayDogs;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryRounds;
import com.grahambartley.dogsunleashed.entity.expedition.ExpeditionHomecoming;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.world.ServerWorld;

public final class AwayDogListener {

  private static final int CHECK_INTERVAL_TICKS = 20;

  private AwayDogListener() {}

  public static void initialize() {
    ServerTickEvents.END_SERVER_TICK.register(
        server -> {
          if (server.getTicks() % CHECK_INTERVAL_TICKS == 0) {
            ExpeditionHomecoming.returnDue(server);
            DeliveryRounds.run(server);
          }
        });
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> {
          if (entity instanceof UnleashedDogEntity dog) {
            discardIfAway(dog, world);
          }
        });
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> DeliveryRounds.recipientLeft(server, handler.getPlayer()));
  }

  public static void discardIfAway(final UnleashedDogEntity dog, final ServerWorld world) {
    if (!AwayDogs.isOffWorld(world.getServer(), dog.getUuid())) {
      return;
    }
    DogsUnleashed.runNextTick(
        () -> {
          if (!dog.isRemoved() && AwayDogs.isOffWorld(world.getServer(), dog.getUuid())) {
            DogsUnleashed.log.warn("[Away] Discarding a stale copy of {}", dog.getUuid());
            dog.discard();
          }
        });
  }
}
