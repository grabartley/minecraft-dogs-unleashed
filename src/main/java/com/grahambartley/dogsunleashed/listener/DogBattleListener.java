package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;

public final class DogBattleListener {

  private DogBattleListener() {}

  public static void initialize() {
    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
        (world, entity, killed) -> {
          if (entity instanceof UnleashedDogEntity dog) {
            dog.getGrowth().recordBattle(killed);
            dog.getPack().recordKill();
          }
        });
  }
}
