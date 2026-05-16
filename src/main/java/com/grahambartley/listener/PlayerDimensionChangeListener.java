package com.grahambartley.listener;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetLocationService;
import com.grahambartley.pet.PetManager;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class PlayerDimensionChangeListener {

  public static void initialize() {
    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) -> {
          final MinecraftServer server = player.getServer();
          final UUID playerId = player.getUuid();
          final String destDim = destination.getRegistryKey().getValue().toString();
          DogsUnleashed.runNextTick(
              () -> {
                final PetManager petManager = PetManager.get(server);
                final ServerWorld dest =
                    server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(destDim)));
                if (dest == null) return;
                for (final PetData petData : petManager.getPetsByOwner(playerId)) {
                  if (!petData.isAlive()) continue;
                  final UnleashedDogEntity dog = PetLocationService.findDog(server, petData);
                  if (dog != null
                      && !dog.isRemoved()
                      && !dog.isInSittingPose()
                      && !dog.isSleepingInBed()) {
                    final ServerPlayerEntity playerEntity =
                        server.getPlayerManager().getPlayer(playerId);
                    if (playerEntity == null) continue;
                    dog.followOwnerToDimension(playerEntity, dest);
                    petData.setDimension(destDim);
                    petData.setLastKnownPosition(playerEntity.getBlockPos());
                    petManager.updatePet(petData);
                  }
                }
              });
        });
  }
}
