package com.grahambartley.dogsunleashed.entity.sled;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.DireWolfGrowthStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import net.minecraft.entity.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledHitchRulesTest {

  @ParameterizedTest(name = "{0}, baby {1}, stage {2} -> {3}")
  @CsvSource({
    "HUSKY, false, YEARLING, true",
    "HUSKY, true, YEARLING, false",
    "DIRE_WOLF, false, PUP, false",
    "DIRE_WOLF, false, YEARLING, false",
    "DIRE_WOLF, false, ADULT, true",
    "DIRE_WOLF, false, PRIME, true",
    "DIRE_WOLF, false, ALPHA, true",
    "BEAGLE, false, YEARLING, false",
    "GOLDEN_RETRIEVER, false, YEARLING, false",
    "SHIBA_INU, false, YEARLING, false",
    "DACHSHUND, false, YEARLING, false",
    "CROSS_BREED, false, YEARLING, false"
  })
  @DisplayName("grown huskies and adult or older dire wolves pull sleds")
  void canPull(
      final UnleashedDogBreed breed,
      final boolean baby,
      final DireWolfGrowthStage stage,
      final boolean expected) {
    assertEquals(expected, SledHitchRules.canPull(breed, baby, stage));
  }

  @ParameterizedTest(name = "{0} hitched -> room for {1}")
  @CsvSource({"0, 6", "4, 2", "6, 0", "7, 0"})
  @DisplayName("a sled takes six dogs at most")
  void room(final int size, final int expected) {
    assertEquals(expected, SledHitchRules.room(size));
  }

  @ParameterizedTest(name = "alive {0}, same owner {1}, leashed {2}, {3} away -> {4}")
  @CsvSource({
    "true, true, false, 5.0, true",
    "true, true, false, 24.0, true",
    "true, true, false, 24.5, false",
    "false, true, false, 5.0, false",
    "true, false, false, 5.0, false",
    "true, true, true, 5.0, false"
  })
  @DisplayName("a dog leaves the team if it dies, changes hands, is leashed or ends up far away")
  void staysHitched(
      final boolean alive,
      final boolean sameOwner,
      final boolean leashed,
      final double distance,
      final boolean expected) {
    assertEquals(expected, SledHitchRules.staysHitched(alive, sameOwner, leashed, distance));
  }

  @ParameterizedTest(name = "{0} -> drops lead {1}")
  @CsvSource({
    "KILLED, true",
    "DISCARDED, true",
    "CHANGED_DIMENSION, true",
    "UNLOADED_TO_CHUNK, false",
    "UNLOADED_WITH_PLAYER, false"
  })
  @DisplayName("a dog that leaves the world drops its lead, one only unloaded keeps its place")
  void dropsLeadOnRemoval(final Entity.RemovalReason reason, final boolean expected) {
    assertEquals(expected, SledHitchRules.dropsLeadOnRemoval(reason));
  }
}
