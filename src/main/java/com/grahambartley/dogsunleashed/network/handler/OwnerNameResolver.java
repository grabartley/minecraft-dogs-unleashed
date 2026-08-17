package com.grahambartley.dogsunleashed.network.handler;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class OwnerNameResolver {

  private OwnerNameResolver() {}

  // Empty when the owner has never been seen by this server's profile cache; the client renders
  // its own unknown-owner fallback so the server never has to localize.
  public static String resolve(final MinecraftServer server, final UUID ownerId) {
    final ServerPlayerEntity onlineOwner = server.getPlayerManager().getPlayer(ownerId);
    if (onlineOwner != null) {
      return onlineOwner.getGameProfile().getName();
    }
    return Optional.ofNullable(server.getUserCache())
        .flatMap(cache -> cache.getByUuid(ownerId))
        .map(GameProfile::getName)
        .orElse("");
  }
}
