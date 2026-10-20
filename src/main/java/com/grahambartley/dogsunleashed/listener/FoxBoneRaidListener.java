package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.burial.BoneThieves;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.passive.FoxEntity;

public final class FoxBoneRaidListener {

  private FoxBoneRaidListener() {}

  public static void initialize() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, damageTaken, blocked) -> {
          if (entity instanceof FoxEntity fox && damageTaken > 0.0F) {
            BoneThieves.dropStolenBone(fox);
          }
        });
  }
}
