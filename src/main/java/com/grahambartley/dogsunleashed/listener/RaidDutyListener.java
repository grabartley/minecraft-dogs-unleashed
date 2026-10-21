package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.DogRaidDuty;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

public final class RaidDutyListener {

  private RaidDutyListener() {}

  public static void initialize() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, damageTaken, blocked) -> {
          if (source.getAttacker() instanceof UnleashedDogEntity dog && damageTaken > 0.0F) {
            dog.getComponents().get(DogRaidDuty.class).recordFight(entity);
          }
        });
  }
}
