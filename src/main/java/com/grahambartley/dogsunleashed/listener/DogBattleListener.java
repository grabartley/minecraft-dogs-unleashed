package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.DogTrustRules;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.entity.player.PlayerEntity;

public final class DogBattleListener {

  private DogBattleListener() {}

  public static void initialize() {
    ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register(
        (world, entity, killed) -> {
          if (entity instanceof UnleashedDogEntity dog) {
            dog.getGrowth().recordBattle(killed);
            dog.getPack().recordKill();
          }
          if (entity instanceof PlayerEntity player) {
            for (final UnleashedDogEntity pup :
                world.getEntitiesByClass(
                    UnleashedDogEntity.class,
                    player.getBoundingBox().expand(DogTrustRules.KILL_RANGE),
                    dog -> dog.getTrust().canBeWon())) {
              pup.getTrust().recordKill(player, killed);
            }
          }
        });
  }
}
