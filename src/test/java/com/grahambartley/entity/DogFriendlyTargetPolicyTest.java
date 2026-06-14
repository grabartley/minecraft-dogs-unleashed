package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

  static Stream<Arguments> friendlyClassificationCases() {
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
  @DisplayName("isFriendlyClass classifies dogs, villagers, and iron golems as friendly")
  void isFriendlyClassClassifiesEntityType(
      final Class<? extends Entity> entityClass, final boolean expectedFriendly) {
    assertEquals(expectedFriendly, DogFriendlyTargetPolicy.isFriendlyClass(entityClass));
  }

  @Test
  @DisplayName("null entity is never a friendly target")
  void nullEntityIsNotAFriendlyTarget() {
    assertFalse(DogFriendlyTargetPolicy.isFriendlyTarget(null));
  }

  @Test
  @DisplayName("null entity class is never friendly")
  void nullEntityClassIsNotFriendly() {
    assertFalse(DogFriendlyTargetPolicy.isFriendlyClass(null));
  }
}
