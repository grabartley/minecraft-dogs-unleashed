package com.grahambartley.dogsunleashed.entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

public final class DireWolfBondListener {

  private DireWolfBondListener() {}

  public static void initialize() {
    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
        (world, entity, killed) -> {
          if (entity instanceof UnleashedDogEntity dog) {
            dog.getGrowth().recordBattle(killed);
          }
        });
  }
}
