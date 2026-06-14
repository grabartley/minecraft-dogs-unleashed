package com.grahambartley.server;

import com.grahambartley.DogsUnleashed;
import com.grahambartley.config.ConfigPaths;
import com.grahambartley.config.DogsUnleashedConfig;
import com.grahambartley.network.ServerConfigPayloads.SyncServerConfigS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

public final class ServerConfigService {
  public static final int OP_PERMISSION_LEVEL = 2;

  private ServerConfigService() {}

  public static void loadFromWorld(final MinecraftServer server) {
    if (server == null) {
      DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.defaults();
      return;
    }
    final ConfigPaths paths = pathsFor(server);
    DogsUnleashed.SERVER_CONFIG = DogsUnleashedConfig.load(paths.getServerConfigPath());
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
