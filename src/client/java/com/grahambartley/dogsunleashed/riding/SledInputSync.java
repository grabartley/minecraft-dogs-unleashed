package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.sled.SledEntity;
import com.grahambartley.dogsunleashed.network.payload.SledSprintPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class SledInputSync {

  private boolean sentHeld;
  private boolean riding;

  public static void register() {
    final SledInputSync sync = new SledInputSync();
    ClientTickEvents.START_CLIENT_TICK.register(sync::tick);
  }

  private void tick(final MinecraftClient client) {
    final boolean onSled =
        client.player != null && client.player.getVehicle() instanceof SledEntity;
    final boolean held = onSled && client.options.sprintKey.isPressed();
    if (onSled && (!this.riding || held != this.sentHeld)) {
      ClientPlayNetworking.send(new SledSprintPayload(held));
      this.sentHeld = held;
    }
    this.riding = onSled;
  }
}
