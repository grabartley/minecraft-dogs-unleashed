package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.away.AwayDogs;
import com.grahambartley.dogsunleashed.entity.delivery.DeliveryRounds;
import com.grahambartley.dogsunleashed.entity.expedition.ExpeditionHomecoming;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
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
    ServerEntityEvents.ENTITY_UNLOAD.register(
        (entity, world) -> {
          if (entity instanceof UnleashedDogEntity dog
              && dog.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK) {
            DeliveryRounds.unloaded(dog, world);
          }
        });
    ServerLivingEntityEvents.AFTER_DEATH.register(
        (entity, source) -> {
          if (entity instanceof UnleashedDogEntity dog
              && dog.getWorld() instanceof ServerWorld world) {
            DeliveryRounds.died(world.getServer(), dog);
          }
        });
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> DeliveryRounds.recipientLeft(server, handler.getPlayer()));
    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) -> DeliveryRounds.recipientLeft(player.getServer(), player));
    ServerPlayerEvents.AFTER_RESPAWN.register(
        (oldPlayer, newPlayer, alive) ->
            DeliveryRounds.recipientLeft(newPlayer.getServer(), newPlayer));
  }

  public static void discardIfAway(final UnleashedDogEntity dog, final ServerWorld world) {
    if (!isStale(dog, world)) {
      return;
    }
    DogsUnleashed.runNextTick(
        () -> {
          if (!dog.isRemoved() && isStale(dog, world)) {
            DogsUnleashed.log.warn("[Away] Discarding a stale copy of {}", dog.getUuid());
            dog.discard();
          }
        });
  }

  private static boolean isStale(final UnleashedDogEntity dog, final ServerWorld world) {
    return AwayDogs.isOffWorld(world.getServer(), dog.getUuid())
        || DeliveryRounds.isStaleCopy(world.getServer(), dog);
  }
}
