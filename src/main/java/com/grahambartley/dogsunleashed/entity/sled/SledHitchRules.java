package com.grahambartley.dogsunleashed.entity.sled;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.entity.Entity;

public final class SledHitchRules {

  public static final double LEAD_REACH = 7.0;
  public static final double BREAK_DISTANCE = 24.0;

  private SledHitchRules() {}

  public static boolean canPull(
      final UnleashedDogBreed breed, final boolean baby, final DireWolfGrowthStage stage) {
    return switch (breed) {
      case HUSKY -> !baby;
      case DIRE_WOLF -> stage.number() >= DireWolfGrowthStage.ADULT.number();
      default -> false;
    };
  }

  public static boolean dropsLeadOnRemoval(final Entity.RemovalReason reason) {
    return reason != Entity.RemovalReason.UNLOADED_TO_CHUNK
        && reason != Entity.RemovalReason.UNLOADED_WITH_PLAYER;
  }

  public static int room(final int teamSize) {
    return Math.max(0, SledFormation.MAX_TEAM - teamSize);
  }

  public static boolean staysHitched(
      final boolean alive,
      final boolean ownedBySameOwner,
      final boolean leashed,
      final boolean hitchedElsewhere,
      final double distanceToSled) {
    return alive
        && ownedBySameOwner
        && !leashed
        && !hitchedElsewhere
        && distanceToSled <= BREAK_DISTANCE;
  }
}
