package com.grahambartley;

import com.grahambartley.listener.PlayerDimensionChangeListener;
import com.grahambartley.network.ModNetworking;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
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

    PlayerDimensionChangeListener.initialize();

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
