package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogJumpPhysicsTest {

  @Test
  @DisplayName("the vanilla jump strength peaks at the well known 1.2522 blocks")
  void vanillaJumpPeak() {
    assertEquals(1.2522, DogJumpPhysics.peakHeight(DogJumpPhysics.VANILLA_JUMP_STRENGTH), 1e-4);
  }

  @Test
  @DisplayName("no upward speed means no height")
  void zeroStrength() {
    assertEquals(0.0, DogJumpPhysics.peakHeight(0.0));
  }

  @ParameterizedTest
  @ValueSource(doubles = {1.0, 1.5, 2.0, 2.5})
  @DisplayName("the strength found for a clearance peaks just above it")
  void strengthToClearPeaksJustAbove(final double clearance) {
    final double peak = DogJumpPhysics.peakHeight(DogJumpPhysics.strengthToClear(clearance));
    assertEquals(clearance + DogJumpPhysics.CLEARANCE_MARGIN, peak, 0.05);
    assertTrue(peak > clearance);
  }

  @Test
  @DisplayName("a higher clearance needs a stronger jump")
  void strengthGrowsWithClearance() {
    assertTrue(DogJumpPhysics.strengthToClear(2.0) > DogJumpPhysics.strengthToClear(1.5));
    assertTrue(DogJumpPhysics.strengthToClear(1.5) > DogJumpPhysics.VANILLA_JUMP_STRENGTH);
  }
}
