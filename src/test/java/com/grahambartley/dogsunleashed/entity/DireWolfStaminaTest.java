package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.config.DireWolfStaminaSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DireWolfStaminaTest {

  private static final DireWolfStaminaSettings DEFAULTS = DireWolfStaminaSettings.DEFAULTS;
  private static final float EPSILON = 1e-4f;

  @Test
  @DisplayName("a prime holds 100 stamina and an alpha 140")
  void capacityPerStage() {
    assertEquals(100.0f, DireWolfStamina.capacity(DireWolfGrowthStage.PRIME, DEFAULTS));
    assertEquals(140.0f, DireWolfStamina.capacity(DireWolfGrowthStage.ALPHA, DEFAULTS));
  }

  @ParameterizedTest
  @EnumSource(
      value = DireWolfGrowthStage.class,
      names = {"PUP", "YEARLING", "ADULT"})
  @DisplayName("stages that can't be ridden have no stamina")
  void youngStagesHaveNone(final DireWolfGrowthStage stage) {
    assertEquals(0.0f, DireWolfStamina.capacity(stage, DEFAULTS));
  }

  @Test
  @DisplayName("the capacity setting scales the stamina pool")
  void capacitySetting() {
    assertEquals(
        250.0f,
        DireWolfStamina.capacity(DireWolfGrowthStage.PRIME, DEFAULTS.withCapacityPercent(250)));
  }

  @Test
  @DisplayName("sprinting drains 0.4 a tick, so a full prime sprints for 12.5 seconds")
  void sprintDrain() {
    DireWolfStamina.State state = new DireWolfStamina.State(100.0f, false);
    int ticks = 0;
    while (state.stamina() > 0.0f) {
      state = DireWolfStamina.next(state, 100.0f, true, true, DEFAULTS);
      ticks++;
    }
    assertEquals(250, ticks);
    assertTrue(state.lockedOut());
  }

  @Test
  @DisplayName("stamina comes back at 0.25 a tick while ridden and 1 a tick at rest")
  void recoveryRates() {
    final DireWolfStamina.State half = new DireWolfStamina.State(50.0f, false);
    assertEquals(
        50.25f, DireWolfStamina.next(half, 100.0f, true, false, DEFAULTS).stamina(), EPSILON);
    assertEquals(
        51.0f, DireWolfStamina.next(half, 100.0f, false, false, DEFAULTS).stamina(), EPSILON);
  }

  @Test
  @DisplayName("the recovery setting scales both recovery rates")
  void recoverySetting() {
    final DireWolfStamina.State half = new DireWolfStamina.State(50.0f, false);
    assertEquals(
        52.0f,
        DireWolfStamina.next(half, 100.0f, false, false, DEFAULTS.withRecoveryPercent(200))
            .stamina(),
        EPSILON);
  }

  @Test
  @DisplayName("with drain turned off, sprinting never costs stamina")
  void drainOff() {
    final DireWolfStamina.State state = new DireWolfStamina.State(30.0f, false);
    assertEquals(
        30.0f,
        DireWolfStamina.next(state, 100.0f, true, true, DEFAULTS.withDrains(false)).stamina(),
        EPSILON);
  }

  @Test
  @DisplayName("an empty wolf can't sprint again until it is back above 25%")
  void lockoutRelease() {
    DireWolfStamina.State state = new DireWolfStamina.State(0.0f, true);
    int ticks = 0;
    while (state.lockedOut()) {
      assertFalse(DireWolfStamina.sprints(state, true, false));
      state = DireWolfStamina.next(state, 100.0f, false, false, DEFAULTS);
      ticks++;
    }
    assertEquals(25, ticks);
    assertTrue(DireWolfStamina.sprints(state, true, false));
  }

  @Test
  @DisplayName("stamina stays between zero and the capacity")
  void clamps() {
    assertEquals(
        100.0f,
        DireWolfStamina.next(
                new DireWolfStamina.State(99.5f, false), 100.0f, false, false, DEFAULTS)
            .stamina(),
        EPSILON);
    assertEquals(
        0.0f,
        DireWolfStamina.next(new DireWolfStamina.State(0.1f, false), 100.0f, true, true, DEFAULTS)
            .stamina(),
        EPSILON);
  }

  @Test
  @DisplayName("there is no sprinting in water or without the key held")
  void sprintConditions() {
    final DireWolfStamina.State full = new DireWolfStamina.State(1.0f, false);
    assertTrue(DireWolfStamina.sprints(full, true, false));
    assertFalse(DireWolfStamina.sprints(full, true, true));
    assertFalse(DireWolfStamina.sprints(full, false, false));
    assertFalse(DireWolfStamina.sprints(new DireWolfStamina.State(0.0f, false), true, false));
  }
}
