package com.grahambartley.dogsunleashed.item;

import com.grahambartley.dogsunleashed.network.payload.WhistleBlowPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public final class WhistleBlowing {

  private WhistleBlowing() {}

  public static void show(final ServerPlayerEntity player, final Hand hand) {
    final WhistleBlowPayload payload = WhistleBlowPayload.of(player.getId(), hand);
    ServerPlayNetworking.send(player, payload);
    for (final ServerPlayerEntity watcher : PlayerLookup.tracking(player)) {
      ServerPlayNetworking.send(watcher, payload);
    }
  }
}
