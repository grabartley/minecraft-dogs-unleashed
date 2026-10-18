package com.grahambartley.dogsunleashed.render.growth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GrowthGroundFitTest {

  @Test
  @DisplayName("unscaled legs need no lift and keep the full height")
  void unscaledLegsNeedNothing() {
    final GrowthGroundFit fit =
        GrowthGroundFit.of(
            List.of(new GrowthGroundFit.Leg(0.4f, 1.0f, 0.4f, 0.0f, 1.0f)), 0.75f, 0.5f, 1.0f);
    assertEquals(0.0f, fit.lift(), 1e-6f);
    assertEquals(1.0f, fit.heightScale(), 1e-6f);
  }

  @Test
  @DisplayName("longer legs lift the body by exactly the extra length")
  void longerLegsLiftTheBody() {
    final GrowthGroundFit fit =
        GrowthGroundFit.of(
            List.of(new GrowthGroundFit.Leg(0.4f, 1.0f, 0.4f, 0.0f, 1.1f)), 0.8f, 0.5f, 1.0f);
    assertEquals(0.04f, fit.lift(), 1e-6f);
    assertEquals(0.8f / 0.84f, fit.heightScale(), 1e-6f);
  }

  @Test
  @DisplayName("shorter legs lower the body so the paws stay on the ground")
  void shorterLegsLowerTheBody() {
    final GrowthGroundFit fit =
        GrowthGroundFit.of(
            List.of(new GrowthGroundFit.Leg(0.4f, 1.0f, 0.4f, 0.0f, 0.8f)), 0.8f, 0.5f, 1.0f);
    assertEquals(-0.08f, fit.lift(), 1e-6f);
  }

  @Test
  @DisplayName("a scaled torso moves the hip, and the longest leg sets the lift")
  void torsoScaleMovesTheHip() {
    final GrowthGroundFit fit =
        GrowthGroundFit.of(
            List.of(
                new GrowthGroundFit.Leg(0.30f, 1.1f, 0.35f, 0.0f, 1.0f),
                new GrowthGroundFit.Leg(0.36f, 1.0f, 0.36f, 0.0f, 1.2f)),
            0.75f,
            0.5f,
            1.0f);
    assertEquals(0.072f, fit.lift(), 1e-6f);
  }

  @Test
  @DisplayName("a bigger head raises the top, which the height scale takes back")
  void biggerHeadIsNormalised() {
    final GrowthGroundFit fit =
        GrowthGroundFit.of(
            List.of(new GrowthGroundFit.Leg(0.4f, 1.0f, 0.4f, 0.0f, 1.0f)), 0.75f, 0.55f, 1.25f);
    assertEquals(0.75f / 0.8f, fit.heightScale(), 1e-6f);
  }
}
