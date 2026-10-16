package com.grahambartley.dogsunleashed.server;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class ServerConfigEditPermission {

  private ServerConfigEditPermission() {}

  public static boolean canEdit(final PlayerEntity player, @Nullable final MinecraftServer server) {
    return player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL)
        || (server != null && server.isHost(player.getGameProfile()));
  }
}
