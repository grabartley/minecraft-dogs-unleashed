package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.goal.DimensionReactionRules;
import com.grahambartley.dogsunleashed.item.pendant.PendantType;
import net.minecraft.world.World;

public final class DogDimensionReaction {

  private DogDimensionReaction() {}

  public static boolean isUneasy(final UnleashedDogEntity dog) {
    return DimensionReactionRules.isUneasy(
        World.NETHER.equals(dog.getWorld().getRegistryKey()),
        dog.getPendants().isWearing(PendantType.GUARDIAN_FIRE));
  }

  public static boolean isInEnd(final UnleashedDogEntity dog) {
    return World.END.equals(dog.getWorld().getRegistryKey());
  }

  public static double followDistanceScale(final UnleashedDogEntity dog) {
    return DimensionReactionRules.followDistanceScale(isUneasy(dog));
  }
}
