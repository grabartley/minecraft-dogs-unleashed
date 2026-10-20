package com.grahambartley.dogsunleashed;

import com.grahambartley.dogsunleashed.advancement.DogExpeditionReturnedCriterion;
import com.grahambartley.dogsunleashed.advancement.DogHowledCriterion;
import com.grahambartley.dogsunleashed.advancement.DogShearBotchedCriterion;
import com.grahambartley.dogsunleashed.advancement.DogSleptInBedCriterion;
import com.grahambartley.dogsunleashed.advancement.DogTamedCriterion;
import com.grahambartley.dogsunleashed.advancement.FetchReturnedCriterion;
import com.grahambartley.dogsunleashed.advancement.PetCuredCriterion;
import com.grahambartley.dogsunleashed.advancement.PetResurrectedCriterion;
import com.grahambartley.dogsunleashed.block.DogBedBlock;
import com.grahambartley.dogsunleashed.command.DogsUnleashedCommand;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.entity.DogPlaySession;
import com.grahambartley.dogsunleashed.entity.expedition.DogExpeditions;
import com.grahambartley.dogsunleashed.entity.scent.ScentRules;
import com.grahambartley.dogsunleashed.gametest.util.GameTestFixturePack;
import com.grahambartley.dogsunleashed.listener.BoneCacheBreakListener;
import com.grahambartley.dogsunleashed.listener.DogBattleListener;
import com.grahambartley.dogsunleashed.listener.DogDimensionChangeListener;
import com.grahambartley.dogsunleashed.listener.ExpeditionListener;
import com.grahambartley.dogsunleashed.listener.OwnerDeathListener;
import com.grahambartley.dogsunleashed.listener.PendantEffectListener;
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
    DogHowledCriterion.register();
    DogTamedCriterion.register();
    FetchReturnedCriterion.register();
    DogSleptInBedCriterion.register();
    DogShearBotchedCriterion.register();
    PetResurrectedCriterion.register();
    PetCuredCriterion.register();
    DogExpeditionReturnedCriterion.register();

    ModSounds.initialize();
    ModComponents.initialize();
    ModBlocks.initialize();
    ModBlockEntities.initialize();
    ModEntities.initialize();
    ModItems.initialize();
    ModPendantItems.initialize();
    ModCosmeticItems.initialize();
    ModCostumeItems.initialize();
    ModArmorMaterials.initialize();
    ModArmorItems.initialize();
    ModScreenHandlers.initialize();
    ModSpawns.initialize();
    ScentRules.register();
    DogExpeditions.registerWheelAction();
    ModNetworking.registerPayloads();
    ModNetworking.registerServerReceivers();
    GameTestFixturePack.registerWhenTesting();

    PlayerDimensionChangeListener.initialize();
    DogDimensionChangeListener.initialize();
    PlayerJoinReunionListener.initialize();
    PetLocationSyncListener.initialize();
    ResurrectionRitualListener.initialize();
    PendantEffectListener.initialize();
    DogBattleListener.initialize();
    OwnerDeathListener.initialize();
    BoneCacheBreakListener.initialize();
    ExpeditionListener.initialize();

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
