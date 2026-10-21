package com.grahambartley.dogsunleashed.network.handler;

import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.network.payload.SledSprintPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class SledNetworkHandler {

  private SledNetworkHandler() {}

  public static void handleSprint(
      final SledSprintPayload payload, final ServerPlayNetworking.Context context) {
    if (context.player().getVehicle() instanceof SledEntity sled) {
      sled.getDrive().setSprintHeld(payload.held());
    }
  }
}
