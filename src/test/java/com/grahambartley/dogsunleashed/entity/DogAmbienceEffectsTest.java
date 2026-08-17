package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogAmbienceEffectsTest {

  private static final BooleanSupplier NEVER = () -> false;
  private static final BooleanSupplier ALWAYS = () -> true;

  static Stream<Arguments> wagInterest() {
    return Stream.of(
        Arguments.of("untamed dog, taming item", false, true, false, true),
        Arguments.of("untamed dog, breeding item", false, false, true, false),
        Arguments.of("tamed dog, breeding item", true, false, true, true),
        Arguments.of("tamed dog, taming item", true, true, false, false),
        Arguments.of("empty handed", false, false, false, false),
        Arguments.of("untamed dog, both items", false, true, true, true),
        Arguments.of("tamed dog, both items", true, true, true, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("wagInterest")
  @DisplayName("an untamed dog wags for a taming item and a tamed one wags for a breeding item")
  void wagInterestDependsOnTameState(
      final String label,
      final boolean tamed,
      final boolean holdingTamingItem,
      final boolean holdingBreedingItem,
      final boolean expected) {
    assertEquals(
        expected,
        DogAmbienceEffects.shouldWagForPlayer(tamed, holdingTamingItem, holdingBreedingItem));
  }

  static Stream<Arguments> tailWagTimers() {
    final int full = DogAmbienceEffects.TAIL_WAG_DURATION_TICKS;
    return Stream.of(
        Arguments.of("interested dog refreshes to full", true, true, 5, NEVER, full),
        Arguments.of("interested dog refreshes even at full", true, true, full, NEVER, full),
        Arguments.of("running timer counts down", true, false, 5, NEVER, 4),
        Arguments.of("idle timer stays at rest without a lucky roll", true, false, 0, NEVER, 0),
        Arguments.of("idle timer starts a wag on a lucky roll", true, false, 0, ALWAYS, full),
        Arguments.of("sitting or angry dog still counts down", false, false, 5, NEVER, 4),
        Arguments.of("sitting or angry dog ignores interest", false, true, 5, NEVER, 4),
        Arguments.of("sitting or angry dog at rest stays at rest", false, false, 0, ALWAYS, 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("tailWagTimers")
  @DisplayName("the tail wag timer refreshes, counts down, or rests according to the dog's state")
  void tailWagTimerTransitions(
      final String label,
      final boolean receptive,
      final boolean shouldWag,
      final int currentTimer,
      final BooleanSupplier randomWagHit,
      final int expected) {
    assertEquals(
        expected,
        DogAmbienceEffects.nextTailWagTimer(receptive, shouldWag, currentTimer, randomWagHit));
  }

  static Stream<Arguments> rngSkippingCases() {
    return Stream.of(
        Arguments.of("dog is interested", true, true, 0),
        Arguments.of("timer is still running", true, false, 5),
        Arguments.of("dog is sitting or angry with a running timer", false, false, 5),
        Arguments.of("dog is sitting or angry at rest", false, false, 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rngSkippingCases")
  @DisplayName("the random wag roll is never drawn outside the one branch that needs it")
  void randomWagRollIsNotDrawnUnnecessarily(
      final String label,
      final boolean receptive,
      final boolean shouldWag,
      final int currentTimer) {
    final AtomicInteger draws = new AtomicInteger();
    DogAmbienceEffects.nextTailWagTimer(
        receptive,
        shouldWag,
        currentTimer,
        () -> {
          draws.incrementAndGet();
          return false;
        });
    assertEquals(0, draws.get());
  }

  @Test
  @DisplayName("the random wag roll is drawn exactly once when the timer is idle and receptive")
  void randomWagRollIsDrawnOnceWhenIdle() {
    final AtomicInteger draws = new AtomicInteger();
    DogAmbienceEffects.nextTailWagTimer(
        true,
        false,
        0,
        () -> {
          draws.incrementAndGet();
          return false;
        });
    assertEquals(1, draws.get());
  }

  static Stream<Arguments> dryingTimers() {
    return Stream.of(
        Arguments.of("submerged resets the timer", true, false, 15, 0),
        Arguments.of("submerged resets a timer that was already counting", true, true, 15, 0),
        Arguments.of("the tick the dog leaves the water starts at one", false, true, 0, 1),
        Arguments.of("a running dry timer keeps climbing", false, false, 5, 6),
        Arguments.of("a dog that was never wet stays at zero", false, false, 0, 0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dryingTimers")
  @DisplayName("the drying timer resets in water, starts on exit, and climbs while dry")
  void dryingTimerTransitions(
      final String label,
      final boolean inWater,
      final boolean wasInWater,
      final int ticksSinceLeftWater,
      final int expected) {
    assertEquals(
        expected,
        DogAmbienceEffects.nextTicksSinceLeftWater(inWater, wasInWater, ticksSinceLeftWater));
  }

  @Test
  @DisplayName("the shake fires on exactly one tick after leaving the water, and never in it")
  void shakeStartsOnASingleDryTick() {
    int startTicks = 0;
    for (int ticks = 0; ticks <= 60; ticks++) {
      if (DogAmbienceEffects.isShakeStartTick(false, ticks)) {
        startTicks++;
      }
      assertFalse(
          DogAmbienceEffects.isShakeStartTick(true, ticks),
          "a submerged dog should never start a shake, ticks=" + ticks);
    }
    assertEquals(1, startTicks);
  }

  @Test
  @DisplayName("shake particles burst on exactly one tick partway through the shake")
  void shakeParticlesBurstOnce() {
    int particleTicks = 0;
    int particleProgress = -1;
    for (int progress = DogAmbienceEffects.SHAKE_DURATION_TICKS; progress > 0; progress--) {
      if (DogAmbienceEffects.isShakeParticleTick(progress)) {
        particleTicks++;
        particleProgress = progress;
      }
    }
    assertEquals(1, particleTicks);
    assertTrue(
        particleProgress > 0 && particleProgress < DogAmbienceEffects.SHAKE_DURATION_TICKS,
        "particles should burst partway through, not on the first or last tick");
  }
}
