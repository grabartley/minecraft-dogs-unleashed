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
          final String originDim = origin.getRegistryKey().getValue().toString();
          final String destDim = destination.getRegistryKey().getValue().toString();
          DogsUnleashed.runNextTick(
              () -> {
                final PetManager petManager = PetManager.get(server);
                final java.util.List<PetData> pets = petManager.getPetsByOwner(playerId);

                final ServerWorld dest =
                    server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(destDim)));
                if (dest == null) {
                  DogsUnleashed.log.warn("[DimChange] Destination world {} not found", destDim);
                  return;
                }
                for (final PetData petData : pets) {
                  if (!petData.isAlive()) {
                    continue;
                  }

                  final UnleashedDogEntity dog = PetLocationService.findDog(server, petData);
                  if (dog == null) {
                    DogsUnleashed.log.warn(
                        "[DimChange] Dog {} not found in any world", petData.getPetId());
                    continue;
                  }
                  if (dog.isRemoved()) {
                    DogsUnleashed.log.warn("[DimChange] Dog {} is removed", petData.getPetId());
                    continue;
                  }
                  if (dog.isInSittingPose()) {
                    continue;
                  }
                  if (dog.isSleepingInBed()) {
                    continue;
                  }

                  final ServerPlayerEntity playerEntity =
                      server.getPlayerManager().getPlayer(playerId);
                  if (playerEntity == null) {
                    DogsUnleashed.log.warn("[DimChange] Player {} not found on server", playerId);
                    continue;
                  }

                  final UnleashedDogEntity teleported =
                      dog.followOwnerToDimension(playerEntity, dest);
                  petData.setDimension(
                      teleported.getWorld().getRegistryKey().getValue().toString());
                  petData.setLastKnownPosition(teleported.getBlockPos());
                  petManager.updatePet(petData);

                  DogsUnleashed.log.info(
                      "[DimChange] Dog {} successfully teleported to {}",
                      petData.getPetId(),
                      teleported.getWorld().getRegistryKey().getValue());
                }
              });
        });
  }
}
