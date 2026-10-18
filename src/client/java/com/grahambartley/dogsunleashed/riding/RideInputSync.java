package com.grahambartley.dogsunleashed.riding;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public final class RideInputSync {

  public static void register() {
    final RideInputSync sync = new RideInputSync();
    ClientTickEvents.START_CLIENT_TICK.register(sync::tick);
  }

  private void tick(final MinecraftClient client) {
    final UnleashedDogEntity dog = RiddenDog.of(client.player);
    if (dog != null) {
      dog.getRiding().setJumpHeld(client.options.jumpKey.isPressed());
    }
  }
}
