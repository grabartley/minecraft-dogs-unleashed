package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DireWolfGrowthStageTest {

  static Stream<Arguments> ladder() {
    return Stream.of(
        Arguments.of(DireWolfGrowthStage.PUP, 1, 0.40f, 0.55f, 0.50f, 20.0, 0.30, 2.0, 0.0, false),
        Arguments.of(
            DireWolfGrowthStage.YEARLING, 2, 0.80f, 1.10f, 1.00f, 30.0, 0.32, 6.0, 2.0, false),
        Arguments.of(
            DireWolfGrowthStage.ADULT, 3, 1.10f, 1.35f, 1.25f, 40.0, 0.34, 8.0, 3.0, false),
        Arguments.of(
            DireWolfGrowthStage.PRIME, 4, 1.40f, 1.60f, 1.45f, 50.0, 0.36, 10.0, 4.0, true),
        Arguments.of(
            DireWolfGrowthStage.ALPHA, 5, 1.80f, 2.50f, 2.25f, 60.0, 0.38, 12.0, 6.0, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ladder")
  @DisplayName("each stage matches the growth table")
  void stageMatchesTheGrowthTable(
      final DireWolfGrowthStage stage,
      final int number,
      final float width,
      final float height,
      final float renderScale,
      final double maxHealth,
      final double speed,
      final double attack,
      final double armor,
      final boolean rideable) {
    assertEquals(number, stage.number());
    assertEquals(width, stage.width(), 1e-6f);
    assertEquals(height, stage.height(), 1e-6f);
    assertEquals(renderScale, stage.renderScaleMultiplier(), 1e-6f);
    assertEquals(maxHealth, stage.maxHealth(), 1e-9);
    assertEquals(speed, stage.movementSpeed(), 1e-9);
    assertEquals(attack, stage.attackDamage(), 1e-9);
    assertEquals(armor, stage.armor(), 1e-9);
    assertEquals(rideable, stage.rideable());
  }

  @ParameterizedTest
  @EnumSource(DireWolfGrowthStage.class)
  @DisplayName("the render scale tracks the hitbox height relative to the yearling")
  void renderScaleTracksHitboxHeight(final DireWolfGrowthStage stage) {
    final float ratio = stage.height() / DireWolfGrowthStage.YEARLING.height();
    assertEquals(ratio, stage.renderScaleMultiplier(), 0.03f, stage.name());
  }

  @ParameterizedTest
  @EnumSource(DireWolfGrowthStage.class)
  @DisplayName("every stage round-trips through its number")
  void stageRoundTripsThroughNumber(final DireWolfGrowthStage stage) {
    assertEquals(stage, DireWolfGrowthStage.fromNumber(stage.number()));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 6, -3})
  @DisplayName("an unknown stage number falls back to the yearling")
  void unknownNumberFallsBackToYearling(final int number) {
    assertEquals(DireWolfGrowthStage.YEARLING, DireWolfGrowthStage.fromNumber(number));
  }

  @Test
  @DisplayName("stages advance in order and stop at the alpha")
  void stagesAdvanceInOrder() {
    assertEquals(DireWolfGrowthStage.YEARLING, DireWolfGrowthStage.PUP.next());
    assertEquals(DireWolfGrowthStage.ADULT, DireWolfGrowthStage.YEARLING.next());
    assertEquals(DireWolfGrowthStage.PRIME, DireWolfGrowthStage.ADULT.next());
    assertEquals(DireWolfGrowthStage.ALPHA, DireWolfGrowthStage.PRIME.next());
    assertNull(DireWolfGrowthStage.ALPHA.next());
  }

  @Test
  @DisplayName("a pup reads as a pup: big head, short legs")
  void pupHasBigHeadAndShortLegs() {
    final DireWolfGrowthStage.SlotScales pup = DireWolfGrowthStage.PUP.slotScales();
    assertTrue(pup.head() > 1.0f);
    assertTrue(pup.legs() < 1.0f);
    assertEquals(DireWolfGrowthStage.SlotScales.NONE, DireWolfGrowthStage.YEARLING.slotScales());
  }

  @Test
  @DisplayName("legs lengthen and the head shrinks relative to the body from yearling to alpha")
  void proportionsChangeMonotonically() {
    float lastLegs = 0.0f;
    float lastHead = Float.MAX_VALUE;
    for (final DireWolfGrowthStage stage : DireWolfGrowthStage.values()) {
      assertTrue(stage.slotScales().legs() > lastLegs, stage + " legs");
      assertTrue(stage.slotScales().head() < lastHead, stage + " head");
      lastLegs = stage.slotScales().legs();
      lastHead = stage.slotScales().head();
    }
  }

  @Test
  @DisplayName("pup to alpha takes three twenty-minute adult stages")
  void adultStagesTakeTwentyMinutes() {
    assertEquals(24000, DireWolfGrowthStage.TICKS_PER_ADULT_STAGE);
  }
}
