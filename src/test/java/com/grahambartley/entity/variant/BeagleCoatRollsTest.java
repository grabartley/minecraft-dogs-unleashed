package com.grahambartley.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeagleCoatRollsTest {

  @Test
  @DisplayName("Natural spawn roll thresholds should map to expected coats")
  void testNaturalRollThresholds() {
    assertEquals(BeagleCoat.TRI_1, BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, 0));
    assertEquals(
        BeagleCoat.TRI_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_1_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.RED_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_1_THRESHOLD));
    assertEquals(
        BeagleCoat.RED_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_RED_1_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.TRI_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_RED_1_THRESHOLD));
    assertEquals(
        BeagleCoat.TRI_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_2_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.TRI_3,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_2_THRESHOLD));
    assertEquals(
        BeagleCoat.TRI_3,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_3_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.RED_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_TRI_3_THRESHOLD));
    assertEquals(
        BeagleCoat.RED_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_RED_2_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.LEMON_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_RED_2_THRESHOLD));
    assertEquals(
        BeagleCoat.LEMON_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_LEMON_1_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.BROWN_WHITE,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_LEMON_1_THRESHOLD));
    assertEquals(
        BeagleCoat.BROWN_WHITE,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_BROWN_WHITE_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.BROWN_WHITE_TAN,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_BROWN_WHITE_THRESHOLD));
    assertEquals(
        BeagleCoat.BROWN_WHITE_TAN,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_BROWN_WHITE_TAN_THRESHOLD - 1));

    assertEquals(
        BeagleCoat.LEMON_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, BeagleCoatRolls.NATURAL_BROWN_WHITE_TAN_THRESHOLD));
    assertEquals(
        BeagleCoat.LEMON_2,
        BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, BeagleCoatRolls.ROLL_BOUND - 1));
  }

  @Test
  @DisplayName("Breeding roll thresholds should include exclusive coats")
  void testBreedingRollThresholds() {
    assertEquals(BeagleCoat.TRI_1, BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, 0));
    assertEquals(
        BeagleCoat.RED_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_TRI_1_THRESHOLD));
    assertEquals(
        BeagleCoat.TRI_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_RED_1_THRESHOLD));
    assertEquals(
        BeagleCoat.BLACK_WHITE,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_TRI_2_THRESHOLD));
    assertEquals(
        BeagleCoat.TRI_3,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_BLACK_WHITE_THRESHOLD));
    assertEquals(
        BeagleCoat.RED_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_TRI_3_THRESHOLD));
    assertEquals(
        BeagleCoat.LEMON_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_RED_2_THRESHOLD));
    assertEquals(
        BeagleCoat.LILAC_1,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_LEMON_1_THRESHOLD));
    assertEquals(
        BeagleCoat.LEMON_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_LILAC_1_THRESHOLD));
    assertEquals(
        BeagleCoat.BROWN_WHITE,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_LEMON_2_THRESHOLD));
    assertEquals(
        BeagleCoat.BROWN_WHITE_TAN,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_BROWN_WHITE_THRESHOLD));
    assertEquals(
        BeagleCoat.LILAC_2,
        BeagleCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, BeagleCoatRolls.BREEDING_BROWN_WHITE_TAN_THRESHOLD));
    assertEquals(
        BeagleCoat.LILAC_2,
        BeagleCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, BeagleCoatRolls.ROLL_BOUND - 1));
  }
}
