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
          DogsUnleashed.log.info(
              "[DimChange] Event fired: player={}, origin={}, dest={}",
              playerId,
              originDim,
              destDim);

          DogsUnleashed.runNextTick(
              () -> {
                DogsUnleashed.log.info(
                    "[DimChange] Deferred task running for player={} in dim={}",
                    playerId,
                    player.getServerWorld().getRegistryKey().getValue());

                final PetManager petManager = PetManager.get(server);
                final java.util.List<PetData> pets = petManager.getPetsByOwner(playerId);
                DogsUnleashed.log.info("[DimChange] Found {} pets for player", pets.size());

                final ServerWorld dest =
                    server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(destDim)));
                if (dest == null) {
                  DogsUnleashed.log.warn("[DimChange] Destination world {} not found", destDim);
                  return;
                }
                for (final PetData petData : pets) {
                  if (!petData.isAlive()) {
                    DogsUnleashed.log.info(
                        "[DimChange] Skipping deceased pet {}", petData.getPetId());
                    continue;
                  }
                  DogsUnleashed.log.info(
                      "[DimChange] Looking for pet {} dim={} pos={}",
                      petData.getPetId(),
                      petData.getDimension(),
                      petData.getLastKnownPosition());

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
                    DogsUnleashed.log.info(
                        "[DimChange] Dog {} is sitting, skipping", petData.getPetId());
                    continue;
                  }
                  if (dog.isSleepingInBed()) {
                    DogsUnleashed.log.info(
                        "[DimChange] Dog {} is sleeping, skipping", petData.getPetId());
                    continue;
                  }

                  final ServerPlayerEntity playerEntity =
                      server.getPlayerManager().getPlayer(playerId);
                  if (playerEntity == null) {
                    DogsUnleashed.log.warn("[DimChange] Player {} not found on server", playerId);
                    continue;
                  }

                  DogsUnleashed.log.info(
                      "[DimChange] Teleporting dog {} from {} to {} at player pos {}",
                      petData.getPetId(),
                      dog.getWorld().getRegistryKey().getValue(),
                      dest.getRegistryKey().getValue(),
                      playerEntity.getBlockPos());

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
