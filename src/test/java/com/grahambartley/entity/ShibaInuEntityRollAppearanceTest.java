package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.entity.variant.ShibaInuCoat;
import com.grahambartley.entity.variant.ShibaInuCoatRolls;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShibaInuEntityRollAppearanceTest {

  @Test
  @DisplayName("Natural shiba roll thresholds should map to expected coats")
  void testNaturalRollThresholds() {
    assertEquals(ShibaInuCoat.RED, ShibaInuCoatRolls.resolveCoatFromRoll(SpawnReason.NATURAL, 0));
    assertEquals(
        ShibaInuCoat.RED,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, ShibaInuCoatRolls.RED_THRESHOLD - 1));
    assertEquals(
        ShibaInuCoat.BLACK,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, ShibaInuCoatRolls.RED_THRESHOLD));
    assertEquals(
        ShibaInuCoat.BLACK,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.NATURAL, ShibaInuCoatRolls.ROLL_BOUND - 1));
  }

  @Test
  @DisplayName("Breeding shiba roll thresholds should include sesame")
  void testBreedingRollThresholds() {
    assertEquals(ShibaInuCoat.RED, ShibaInuCoatRolls.resolveCoatFromRoll(SpawnReason.BREEDING, 0));
    assertEquals(
        ShibaInuCoat.BLACK,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, ShibaInuCoatRolls.RED_THRESHOLD));
    assertEquals(
        ShibaInuCoat.SESAME,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, ShibaInuCoatRolls.BLACK_THRESHOLD));
    assertEquals(
        ShibaInuCoat.SESAME,
        ShibaInuCoatRolls.resolveCoatFromRoll(
            SpawnReason.BREEDING, ShibaInuCoatRolls.ROLL_BOUND - 1));
  }
}
