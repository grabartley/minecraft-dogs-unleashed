package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.advancement.DogSleptInBedCriterion;
import com.grahambartley.dogsunleashed.advancement.FetchReturnedCriterion;
import com.grahambartley.dogsunleashed.advancement.HuskyHowledCriterion;
import com.grahambartley.dogsunleashed.advancement.PetCuredCriterion;
import com.grahambartley.dogsunleashed.advancement.PetResurrectedCriterion;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.command.DogsUnleashedCommand;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogPlaySession;
import com.grahambartley.dogsunleashed.listener.PetLocationSyncListener;
import com.grahambartley.dogsunleashed.listener.PlayerDimensionChangeListener;
import com.grahambartley.dogsunleashed.listener.PlayerJoinReunionListener;
import com.grahambartley.dogsunleashed.listener.ResurrectionRitualListener;
import com.grahambartley.dogsunleashed.network.ModNetworking;
import com.grahambartley.dogsunleashed.server.NextTickQueue;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
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

  private static final NextTickQueue NEXT_TICK_QUEUE = new NextTickQueue();

  @Override
  public void onInitialize() {
    HuskyHowledCriterion.register();
    FetchReturnedCriterion.register();
    DogSleptInBedCriterion.register();
    PetResurrectedCriterion.register();
    PetCuredCriterion.register();

    ModSounds.initialize();
    ModComponents.initialize();
    ModBlocks.initialize();
    ModBlockEntities.initialize();
    ModEntities.initialize();
    ModItems.initialize();
    ModScreenHandlers.initialize();
    ModSpawns.initialize();
    ModNetworking.registerPayloads();
    ModNetworking.registerServerReceivers();

    PlayerDimensionChangeListener.initialize();
    PlayerJoinReunionListener.initialize();
    PetLocationSyncListener.initialize();
    ResurrectionRitualListener.initialize();

    ServerLifecycleEvents.SERVER_STARTING.register(ServerConfigService::loadFromWorld);

    ServerLifecycleEvents.SERVER_STOPPED.register(DogsUnleashed::clearJvmGlobalSessionState);

    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> ServerConfigService.sendTo(handler.getPlayer()));

    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> DogsUnleashedCommand.register(dispatcher));

    ServerTickEvents.START_SERVER_TICK.register(server -> NEXT_TICK_QUEUE.runPending());

    log.info("Dogs Unleashed loaded successfully");
  }

  public static void runNextTick(Runnable task) {
    NEXT_TICK_QUEUE.enqueue(task);
  }

  private static void clearJvmGlobalSessionState(MinecraftServer server) {
    DogPlaySession.clearActivePlaySessions();
    DogBedBlock.clearPendingAssignments();
    NEXT_TICK_QUEUE.clear();
  }
}
