package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.RideSprintPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class RideSprintNetworkHandler {

  private RideSprintNetworkHandler() {}

  public static void handleRideSprint(
      final RideSprintPayload payload, final ServerPlayNetworking.Context context) {
    applySprint(context.player(), payload.sprinting());
  }

  public static boolean applySprint(final ServerPlayerEntity player, final boolean sprinting) {
    if (!(player.getVehicle() instanceof UnleashedDogEntity dog)
        || dog.getRiding().rider() != player) {
      return false;
    }
    dog.getRiding().setSprintHeld(sprinting);
    return true;
  }
}
