package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.entity.variant.HuskyCoat;
import com.grahambartley.entity.variant.HuskyCoatRolls;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyEntityRollAppearanceTest {

  @Test
  @DisplayName("Natural husky roll thresholds should map to expected coats")
  void testNaturalRollThresholds() {
    assertEquals(HuskyCoat.BLACK_WHITE, HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, 0));
    assertEquals(
        HuskyCoat.BLACK_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD - 1));

    assertEquals(
        HuskyCoat.GREY_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.GREY_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD - 1));

    assertEquals(
        HuskyCoat.RED_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.RED_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD - 1));

    assertEquals(
        HuskyCoat.SABLE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.SABLE,
        HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, HuskyCoatRolls.ROLL_BOUND - 1));
  }

  @Test
  @DisplayName("Breeding husky roll thresholds should include exclusive coats")
  void testBreedingRollThresholds() {
    assertEquals(
        HuskyCoat.BLACK_WHITE, HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, 0));
    assertEquals(
        HuskyCoat.GREY_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, HuskyCoatRolls.NATURAL_BLACK_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.RED_WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, HuskyCoatRolls.NATURAL_GREY_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.SABLE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, HuskyCoatRolls.NATURAL_RED_WHITE_THRESHOLD));
    assertEquals(
        HuskyCoat.AGOUTI,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, HuskyCoatRolls.BREEDING_SABLE_THRESHOLD));
    assertEquals(
        HuskyCoat.WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, HuskyCoatRolls.BREEDING_AGOUTI_THRESHOLD));
    assertEquals(
        HuskyCoat.WHITE,
        HuskyCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, HuskyCoatRolls.ROLL_BOUND - 1));
  }
}
