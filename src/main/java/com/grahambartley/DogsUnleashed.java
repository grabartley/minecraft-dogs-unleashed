package com.grahambartley;

import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.pet.PetData;
import com.grahambartley.pet.PetManager;
import java.util.ArrayList;
import java.util.Iterator;
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

  private static final List<ScheduledTask> scheduledTasks = new ArrayList<>();
  private static long serverTickCounter = 0L;

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
          serverTickCounter++;
          if (!scheduledTasks.isEmpty()) {
            final List<Runnable> readyTasks = new ArrayList<>();
            final Iterator<ScheduledTask> iterator = scheduledTasks.iterator();
            while (iterator.hasNext()) {
              final ScheduledTask task = iterator.next();
              if (task.runAtTick <= serverTickCounter) {
                readyTasks.add(task.task);
                iterator.remove();
              }
            }
            for (final Runnable task : readyTasks) {
              task.run();
            }
          }
        });

    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) -> {
          final MinecraftServer server = player.getServer();
          final java.util.UUID playerId = player.getUuid();
          final String destDim = destination.getRegistryKey().getValue().toString();
          scheduleInTicks(
              1,
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
                  UnleashedDogEntity.findAndLoadWithTicket(
                      server,
                      petData,
                      dog -> {
                        if (dog.isRemoved() || dog.isInSittingPose() || dog.isSleepingInBed()) {
                          return;
                        }
                        final net.minecraft.server.network.ServerPlayerEntity playerEntity =
                            server.getPlayerManager().getPlayer(playerId);
                        if (playerEntity == null) return;
                        dog.followOwnerToDimension(playerEntity, dest);
                        petData.setDimension(destDim);
                        petData.setLastKnownPosition(playerEntity.getBlockPos());
                        petManager.updatePet(petData);
                      },
                      () -> {});
                }
              });
        });

    log.info("Dogs Unleashed loaded successfully");
  }

  public static void scheduleInTicks(int delayTicks, Runnable task) {
    scheduledTasks.add(new ScheduledTask(serverTickCounter + Math.max(1, delayTicks), task));
  }

  private static final class ScheduledTask {
    private final long runAtTick;
    private final Runnable task;

    private ScheduledTask(long runAtTick, Runnable task) {
      this.runAtTick = runAtTick;
      this.task = task;
    }
  }
}
