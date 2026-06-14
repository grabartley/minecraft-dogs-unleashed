package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogFriendlyTargetPolicyTest {

  @Test
  @DisplayName("Friendly target list contains other dogs and villagers")
  void friendlyTargetListContainsDogsAndVillagers() {
    assertEquals(2, DogFriendlyTargetPolicy.FRIENDLY_TARGET_TYPES.length);
    assertTrue(containsType(UnleashedDogEntity.class));
    assertTrue(containsType(VillagerEntity.class));
  }

  @Test
  @DisplayName("Every dog breed subclass is treated as friendly")
  void allDogBreedSubclassesAreFriendly() {
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(UnleashedDogEntity.class));
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(BeagleEntity.class));
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(DachshundEntity.class));
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(HuskyEntity.class));
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(GoldenRetrieverEntity.class));
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(ShibaInuEntity.class));
  }

  @Test
  @DisplayName("Villagers are treated as friendly")
  void villagersAreFriendly() {
    assertTrue(DogFriendlyTargetPolicy.isFriendlyClass(VillagerEntity.class));
  }

  @Test
  @DisplayName("Hostile mobs are not treated as friendly")
  void hostileMobsAreNotFriendly() {
    assertFalse(DogFriendlyTargetPolicy.isFriendlyClass(ZombieEntity.class));
  }

  @Test
  @DisplayName("Players are not treated as friendly so owner anger logic still applies")
  void playersAreNotFriendly() {
    assertFalse(DogFriendlyTargetPolicy.isFriendlyClass(PlayerEntity.class));
  }

  @Test
  @DisplayName("Null entity is not friendly")
  void nullEntityIsNotFriendly() {
    assertFalse(DogFriendlyTargetPolicy.isFriendlyTarget(null));
    assertFalse(DogFriendlyTargetPolicy.isFriendlyClass(null));
  }

  private static boolean containsType(Class<? extends Entity> type) {
    for (Class<?> friendlyType : DogFriendlyTargetPolicy.FRIENDLY_TARGET_TYPES) {
      if (friendlyType.equals(type)) {
        return true;
      }
    }
    return false;
  }
}
