package com.grahambartley.listener;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.entity.UnleashedDogEntity;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public final class PlayerJoinReunionListener {

  private static final double REUNION_SEARCH_RADIUS = 32.0D;

  private PlayerJoinReunionListener() {}

  public static void initialize() {
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> {
          final UUID playerId = handler.getPlayer().getUuid();
          // Deferred a tick so the join player's surrounding chunks and entity tracking are live,
          // otherwise the nearby-dog query can miss dogs that have not finished loading yet.
          DogsUnleashed.runNextTick(() -> celebrateNearbyDogs(server, playerId));
        });
  }

  public static void celebrateNearbyDogs(final MinecraftServer server, final UUID playerId) {
    final ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
    if (player == null) {
      return;
    }
    celebrateDogsNear(player);
  }

  public static void celebrateDogsNear(final ServerPlayerEntity player) {
    if (!(player.getWorld() instanceof ServerWorld serverWorld)) {
      return;
    }

    final Box searchBox = player.getBoundingBox().expand(REUNION_SEARCH_RADIUS);
    final List<UnleashedDogEntity> dogs =
        serverWorld.getEntitiesByClass(
            UnleashedDogEntity.class,
            searchBox,
            dog -> dog.isAlive() && dog.isTamed() && dog.isOwner(player));

    for (final UnleashedDogEntity dog : dogs) {
      dog.celebrateOwnerArrival();
    }
  }
}
