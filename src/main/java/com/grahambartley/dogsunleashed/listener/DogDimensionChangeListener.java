package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;

public final class DogDimensionChangeListener {

  private DogDimensionChangeListener() {}

  public static void initialize() {
    ServerEntityWorldChangeEvents.AFTER_ENTITY_CHANGE_WORLD.register(
        (original, moved, origin, destination) -> {
          if (moved instanceof UnleashedDogEntity dog) {
            dog.getCommandController().anchorWhereItArrived();
          }
        });
  }
}
