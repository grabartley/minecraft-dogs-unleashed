package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.entity.variant.DachshundCoat;
import com.grahambartley.entity.variant.DachshundCoatRolls;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DachshundEntityRollAppearanceTest {

  @Test
  @DisplayName("Natural spawn roll thresholds should map to expected coats")
  void testNaturalRollThresholds() {
    assertEquals(
        DachshundCoat.BLACK_TAN, DachshundCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, 0));
    assertEquals(
        DachshundCoat.BLACK_TAN,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_BLACK_TAN_THRESHOLD - 1));

    assertEquals(
        DachshundCoat.RED,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_BLACK_TAN_THRESHOLD));
    assertEquals(
        DachshundCoat.RED,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_RED_THRESHOLD - 1));

    assertEquals(
        DachshundCoat.CHOCOLATE_TAN,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_RED_THRESHOLD));
    assertEquals(
        DachshundCoat.CHOCOLATE_TAN,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_CHOCOLATE_TAN_THRESHOLD - 1));

    assertEquals(
        DachshundCoat.CHOCOLATE_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_CHOCOLATE_TAN_THRESHOLD));
    assertEquals(
        DachshundCoat.CHOCOLATE_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_CHOCOLATE_CREAM_THRESHOLD - 1));

    assertEquals(
        DachshundCoat.BLACK_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_CHOCOLATE_CREAM_THRESHOLD));
    assertEquals(
        DachshundCoat.BLACK_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_BLACK_CREAM_THRESHOLD - 1));

    assertEquals(
        DachshundCoat.RED_PIEBALD,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.NATURAL_BLACK_CREAM_THRESHOLD));
    assertEquals(
        DachshundCoat.RED_PIEBALD,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, DachshundCoatRolls.ROLL_BOUND - 1));
  }

  @Test
  @DisplayName("Breeding roll thresholds should include exclusive coats")
  void testBreedingRollThresholds() {
    assertEquals(
        DachshundCoat.BLACK_TAN, DachshundCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, 0));
    assertEquals(
        DachshundCoat.RED,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_BLACK_TAN_THRESHOLD));
    assertEquals(
        DachshundCoat.CHOCOLATE_TAN,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_RED_THRESHOLD));
    assertEquals(
        DachshundCoat.CHOCOLATE_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_CHOCOLATE_TAN_THRESHOLD));
    assertEquals(
        DachshundCoat.BLACK_CREAM,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_CHOCOLATE_CREAM_THRESHOLD));
    assertEquals(
        DachshundCoat.RED_PIEBALD,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_BLACK_CREAM_THRESHOLD));
    assertEquals(
        DachshundCoat.BLACK_TAN_PIEBALD,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_RED_PIEBALD_THRESHOLD));
    assertEquals(
        DachshundCoat.BLUE_TAN,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_BLACK_TAN_PIEBALD_THRESHOLD));
    assertEquals(
        DachshundCoat.ALBINO,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.BREEDING_BLUE_TAN_THRESHOLD));
    assertEquals(
        DachshundCoat.ALBINO,
        DachshundCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, DachshundCoatRolls.ROLL_BOUND - 1));
  }
}
