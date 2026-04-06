package com.grahambartley;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DogsUnleashed implements ModInitializer {
  public static final String MOD_ID = "dogs-unleashed";

  public static final Logger log = LoggerFactory.getLogger(MOD_ID);

  // Tasks deferred to the start of the next server tick to avoid portal-mechanics race conditions.
  private static final List<Runnable> pendingNextTick = new ArrayList<>();

  @Override
  public void onInitialize() {
    ModSounds.initialize();
    ModComponents.initialize();
    ModBlocks.initialize();
    ModBlockEntities.initialize();
    ModEntities.initialize();
    ModItems.initialize();
    ModSpawns.initialize();
    ModNetworking.registerPayloads();
    ModNetworking.registerServerReceivers();

    ServerTickEvents.START_SERVER_TICK.register(
        (MinecraftServer server) -> {
          if (!pendingNextTick.isEmpty()) {
            final List<Runnable> tasks = new ArrayList<>(pendingNextTick);
            pendingNextTick.clear();
            for (final Runnable task : tasks) {
              task.run();
            }
          }
        });

    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) -> {
          final MinecraftServer server = player.getServer();
          final java.util.UUID playerId = player.getUuid();
          final String destDim = destination.getRegistryKey().getValue().toString();
          pendingNextTick.add(
              () -> {
                final PetManager petManager = PetManager.get(server);
                final ServerWorld dest =
                    server.getWorld(
                        net.minecraft.registry.RegistryKey.of(
                            net.minecraft.registry.RegistryKeys.WORLD,
                            net.minecraft.util.Identifier.of(destDim)));
                if (dest == null) return;
                for (final PetData petData : petManager.getPetsByOwner(playerId)) {
                  if (!petData.isAlive()) continue;
                  final UnleashedDogEntity dog = UnleashedDogEntity.findAndLoad(server, petData);
                  if (dog != null
                      && !dog.isRemoved()
                      && !dog.isInSittingPose()
                      && !dog.isSleepingInBed()) {
                    final net.minecraft.server.network.ServerPlayerEntity playerEntity =
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

    log.info("Dogs Unleashed loaded successfully");
  }

  public static void runNextTick(Runnable task) {
    pendingNextTick.add(task);
  }
}
