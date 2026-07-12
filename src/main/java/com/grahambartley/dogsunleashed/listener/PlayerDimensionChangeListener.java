package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.pet.PetLocationService;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;

public final class PlayerDimensionChangeListener {

  private PlayerDimensionChangeListener() {}

  public static void initialize() {
    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) ->
            // Deferred a tick so the destination world has fully accepted the player before pets
            // are located and summoned.
            DogsUnleashed.runNextTick(() -> PetLocationService.bringActivePetsToOwner(player)));
  }
}
