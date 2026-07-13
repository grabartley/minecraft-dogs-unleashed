package com.grahambartley.dogsunleashed.server;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.config.ConfigPaths;
import com.grahambartley.dogsunleashed.config.DogsUnleashedConfig;
import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import java.nio.file.Path;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerConfigService {
  public static final int OP_PERMISSION_LEVEL = 2;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigService.class);

  private ServerConfigService() {}

  /**
   * Loads the world's config the moment its save session opens, which happens before {@code
   * MinecraftServer} is constructed. Fabric bakes biome modifications inside the server
   * constructor, so spawn weights resolved there only see the world's saved values because this ran
   * first; the {@code SERVER_STARTING} reload alone would be too late.
   */
  public static void loadFromSession(final LevelStorage.Session session) {
    if (session == null) {
      DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.defaults();
      return;
    }
    loadFromPath(session.getDirectory(WorldSavePath.ROOT).normalize());
  }

  public static void loadFromWorld(final MinecraftServer server) {
    if (server == null) {
      DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.defaults();
      return;
    }
    loadFromPath(server.getSavePath(WorldSavePath.ROOT).normalize());
  }

  private static void loadFromPath(final Path worldRoot) {
    final ConfigPaths paths = new ConfigPaths(worldRoot);
    DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.load(paths.getServerConfigPath());
    LOGGER.info(
        "Dogs Unleashed server config loaded for {}: spawn={}, spawnrate={}%, breeds={}, "
            + "capindependentspawning={}",
        worldRoot.getFileName(),
        DogsUnleashed.SERVER_CONFIG.enableNaturalSpawning(),
        DogsUnleashed.SERVER_CONFIG.spawnRateMultiplierPercent(),
        DogsUnleashed.SERVER_CONFIG.breedSpawnRateMultipliersPercent(),
        DogsUnleashed.SERVER_CONFIG.capIndependentSpawningEnabled());
  }

  public static boolean update(final MinecraftServer server, final DogsUnleashedConfig updated) {
    if (server == null || updated == null) {
      return false;
    }
    final ConfigPaths paths = pathsFor(server);
    if (!DogsUnleashedConfig.save(paths.getServerConfigPath(), updated)) {
      return false;
    }
    DogsUnleashed.SERVER_CONFIG = updated;
    broadcast(server, updated);
    return true;
  }

  public static void sendTo(final ServerPlayerEntity player) {
    ServerPlayNetworking.send(player, new SyncServerConfigS2CPayload(DogsUnleashed.SERVER_CONFIG));
  }

  public static void broadcast(final MinecraftServer server, final DogsUnleashedConfig config) {
    if (server == null) {
      return;
    }
    final SyncServerConfigS2CPayload payload = new SyncServerConfigS2CPayload(config);
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      ServerPlayNetworking.send(player, payload);
    }
  }

  private static ConfigPaths pathsFor(final MinecraftServer server) {
    return new ConfigPaths(server.getSavePath(WorldSavePath.ROOT).normalize());
  }
}
