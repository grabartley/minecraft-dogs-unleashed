package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.network.ServerConfigPayloads.EditServerConfigC2SPayload;
import com.grahambartley.dogsunleashed.server.ServerConfigService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerConfigNetworkHandler {

  private ServerConfigNetworkHandler() {}

  public static void handleEditServerConfig(
      final EditServerConfigC2SPayload payload, final ServerPlayNetworking.Context context) {
    final ServerPlayerEntity player = context.player();
    player
        .getServer()
        .execute(
            () -> {
              if (!player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL)) {
                ServerConfigService.sendTo(player);
                return;
              }
              ServerConfigService.update(player.getServer(), payload.config());
            });
  }
}
