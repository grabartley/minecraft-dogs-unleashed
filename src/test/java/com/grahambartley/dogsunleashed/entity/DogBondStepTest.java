package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class DogBondStepTest {

  @ParameterizedTest(name = "{0} points -> {1}")
  @CsvSource({
    "-5, STRANGER",
    "0, STRANGER",
    "49, STRANGER",
    "50, FAMILIAR",
    "149, FAMILIAR",
    "150, FRIEND",
    "349, FRIEND",
    "350, COMPANION",
    "699, COMPANION",
    "700, DEVOTED",
    "5000, DEVOTED"
  })
  @DisplayName("points reach the highest step whose threshold they meet")
  void ofPoints(final int points, final DogBondStep expected) {
    assertEquals(expected, DogBondStep.ofPoints(points));
  }

  @ParameterizedTest(name = "id {0} -> {1}")
  @CsvSource({"0, STRANGER", "2, FRIEND", "4, DEVOTED", "-1, STRANGER", "5, STRANGER"})
  @DisplayName("a synced id maps back to its step, and an unknown id to Stranger")
  void byId(final int id, final DogBondStep expected) {
    assertEquals(expected, DogBondStep.byId(id));
  }

  @ParameterizedTest(name = "{0} raised by {1} -> {2}")
  @CsvSource({
    "STRANGER, 0, STRANGER",
    "STRANGER, 1, FAMILIAR",
    "COMPANION, 1, DEVOTED",
    "DEVOTED, 1, DEVOTED",
    "FRIEND, -2, FRIEND"
  })
  @DisplayName("raising a step stops at Devoted and never lowers it")
  void raisedBy(final DogBondStep step, final int steps, final DogBondStep expected) {
    assertEquals(expected, step.raisedBy(steps));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogBondStep.class)
  @DisplayName(
      "each step after Stranger wags faster, follows closer, keeps up from farther within the 16 block path range, and responds faster")
  void rewardsGrowWithEachStep(final DogBondStep step) {
    if (step == DogBondStep.STRANGER) {
      assertEquals(1.0, step.wagSpeed());
      assertEquals(10.0, step.followDistance(10.0));
      assertEquals(12.0, step.teleportDistance());
      assertEquals(1.0, step.responseSpeed(1.0));
      return;
    }
    final DogBondStep previous = DogBondStep.byId(step.ordinal() - 1);
    assertTrue(step.minPoints() > previous.minPoints());
    assertTrue(step.wagSpeed() > previous.wagSpeed());
    assertTrue(step.followDistance(10.0) < previous.followDistance(10.0));
    assertTrue(step.teleportDistance() > previous.teleportDistance());
    assertTrue(step.teleportDistance() <= 16.0);
    assertTrue(step.responseSpeed(1.0) > previous.responseSpeed(1.0));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "STRANGER, screen.dogs-unleashed.pet_details.bond_stranger",
    "FAMILIAR, screen.dogs-unleashed.pet_details.bond_familiar",
    "FRIEND, screen.dogs-unleashed.pet_details.bond_friend",
    "COMPANION, screen.dogs-unleashed.pet_details.bond_companion",
    "DEVOTED, screen.dogs-unleashed.pet_details.bond_devoted"
  })
  @DisplayName("each step names itself through its own translation key")
  void translationKey(final DogBondStep step, final String expected) {
    assertEquals(expected, step.translationKey());
  }
}
