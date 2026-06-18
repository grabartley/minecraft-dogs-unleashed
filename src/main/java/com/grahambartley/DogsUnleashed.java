package com.grahambartley;

import com.grahambartley.advancement.DogSleptInBedCriterion;
import com.grahambartley.advancement.FetchReturnedCriterion;
import com.grahambartley.advancement.HuskyHowledCriterion;
import com.grahambartley.block.DogBedBlock;
import com.grahambartley.command.DogsUnleashedCommand;
import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.entity.UnleashedDogEntity;
import com.grahambartley.listener.PlayerDimensionChangeListener;
import com.grahambartley.network.ModNetworking;
import com.grahambartley.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DogsUnleashed implements ModInitializer {
  public static final String MOD_ID = "dogs-unleashed";

  public static final Logger log = LoggerFactory.getLogger(MOD_ID);

  public static volatile DogsUnleashedConfig SERVER_CONFIG = DogsUnleashedConfig.defaults();

  // Tasks deferred to the start of the next server tick to avoid portal-mechanics race conditions.
  private static final List<Runnable> pendingNextTick = new ArrayList<>();

  @Override
  public void onInitialize() {
    HuskyHowledCriterion.register();
    FetchReturnedCriterion.register();
    DogSleptInBedCriterion.register();

    ModSounds.initialize();
    ModComponents.initialize();
    ModBlocks.initialize();
    ModBlockEntities.initialize();
    ModEntities.initialize();
    ModItems.initialize();
    ModSpawns.initialize();
    ModNetworking.registerPayloads();
    ModNetworking.registerServerReceivers();

    PlayerDimensionChangeListener.initialize();

    ServerLifecycleEvents.SERVER_STARTING.register(ServerConfigService::loadFromWorld);

    // Clear JVM-global session maps on stop so a subsequent integrated server start
    // (singleplayer world load in the same JVM) begins with empty session state. See #176.
    ServerLifecycleEvents.SERVER_STOPPED.register(
        (server) -> {
          UnleashedDogEntity.clearActivePlaySessions();
          DogBedBlock.clearPendingAssignments();
        });

    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> ServerConfigService.sendTo(handler.getPlayer()));

    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> DogsUnleashedCommand.register(dispatcher));

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

    log.info("Dogs Unleashed loaded successfully");
  }

  public static void runNextTick(Runnable task) {
    pendingNextTick.add(task);
  }
}
