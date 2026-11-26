package com.grahambartley.dogsunleashed.gesture;

import com.grahambartley.dogsunleashed.network.payload.PlayerGesturePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

public final class PlayerGestures {

  private PlayerGestures() {}

  public static void show(
      final ServerPlayerEntity player, final Hand hand, final PlayerGesture gesture) {
    final PlayerGesturePayload payload = PlayerGesturePayload.of(player.getId(), hand, gesture);
    ServerPlayNetworking.send(player, payload);
    for (final ServerPlayerEntity watcher : PlayerLookup.tracking(player)) {
      ServerPlayNetworking.send(watcher, payload);
    }
  }
}
