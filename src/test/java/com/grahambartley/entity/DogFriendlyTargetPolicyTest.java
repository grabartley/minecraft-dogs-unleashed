package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogFriendlyTargetPolicyTest {

  private static Stream<Arguments> friendlyClassificationCases() {
    return Stream.of(
        Arguments.of(UnleashedDogEntity.class, true),
        Arguments.of(BeagleEntity.class, true),
        Arguments.of(DachshundEntity.class, true),
        Arguments.of(HuskyEntity.class, true),
        Arguments.of(GoldenRetrieverEntity.class, true),
        Arguments.of(ShibaInuEntity.class, true),
        Arguments.of(VillagerEntity.class, true),
        Arguments.of(IronGolemEntity.class, true),
        Arguments.of(ZombieEntity.class, false),
        Arguments.of(PlayerEntity.class, false));
  }

  @ParameterizedTest(name = "{0} friendly={1}")
  @MethodSource("friendlyClassificationCases")
  @DisplayName("isFriendlyClass classifies entity types correctly")
  void isFriendlyClassCases(Class<? extends Entity> entityClass, boolean expectedFriendly) {
    assertEquals(expectedFriendly, DogFriendlyTargetPolicy.isFriendlyClass(entityClass));
  }

  @Test
  @DisplayName("Friendly target list contains other dogs, villagers, and iron golems")
  void friendlyTargetListContainsExpectedTypes() {
    assertEquals(3, DogFriendlyTargetPolicy.FRIENDLY_TARGET_TYPES.length);
    assertTrue(containsType(UnleashedDogEntity.class));
    assertTrue(containsType(VillagerEntity.class));
    assertTrue(containsType(IronGolemEntity.class));
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
