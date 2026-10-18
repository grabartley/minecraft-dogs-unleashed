package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.network.payload.RideSprintPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public final class RideInputSync {

  private boolean sentSprint;

  public static void register() {
    final RideInputSync sync = new RideInputSync();
    ClientTickEvents.START_CLIENT_TICK.register(sync::tick);
  }

  private void tick(final MinecraftClient client) {
    final UnleashedDogEntity dog = RiddenDog.of(client.player);
    if (dog == null) {
      this.sentSprint = false;
      return;
    }
    final boolean sprint = client.options.sprintKey.isPressed();
    dog.getRiding().setRiderInput(sprint, client.options.jumpKey.isPressed());
    if (sprint != this.sentSprint) {
      this.sentSprint = sprint;
      ClientPlayNetworking.send(new RideSprintPayload(sprint));
    }
  }
}
