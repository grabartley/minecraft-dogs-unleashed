package com.grahambartley.dogsunleashed.listener;

import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.burial.BoneThieves;
import com.grahambartley.dogsunleashed.entity.goal.FoxRaidCacheGoal;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.passive.FoxEntity;

public final class FoxBoneRaidListener {

  static final int FLEE_GOAL_PRIORITY = 4;
  static final int RAID_GOAL_PRIORITY = 6;
  static final float FLEE_DISTANCE = 8.0F;
  static final double SLOW_SPEED = 1.6;
  static final double FAST_SPEED = 1.4;

  private FoxBoneRaidListener() {}

  public static void initialize() {
    ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> addGoals(entity));
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (entity, source, baseDamage, damageTaken, blocked) -> {
          if (entity instanceof FoxEntity fox && damageTaken > 0.0F) {
            BoneThieves.dropStolenBone(fox);
          }
        });
  }

  static void addGoals(final Entity entity) {
    if (entity instanceof FoxEntity fox) {
      fox.goalSelector.add(
          FLEE_GOAL_PRIORITY,
          new FleeEntityGoal<>(
              fox,
              UnleashedDogEntity.class,
              FLEE_DISTANCE,
              SLOW_SPEED,
              FAST_SPEED,
              dog -> ((UnleashedDogEntity) dog).isTamed() && BoneThieves.isThief(fox)));
      fox.goalSelector.add(RAID_GOAL_PRIORITY, new FoxRaidCacheGoal(fox));
    }
  }
}
