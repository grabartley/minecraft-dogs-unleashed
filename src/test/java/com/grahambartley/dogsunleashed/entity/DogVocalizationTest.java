package com.grahambartley.dogsunleashed.entity;

import static com.grahambartley.dogsunleashed.ModConstants.BARK_PITCH;
import static com.grahambartley.dogsunleashed.ModConstants.FULL_MOON_PHASE;
import static com.grahambartley.dogsunleashed.ModConstants.LOW_HEALTH_THRESHOLD;
import static com.grahambartley.dogsunleashed.ModConstants.PUPPY_BARK_PITCH_MULTIPLIER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogVocalizationTest {

  private static final float MAX_HEALTH = 20.0f;
  private static final int NEW_MOON_PHASE = 4;

  @Test
  @DisplayName("an adult barks at the base pitch")
  void adultBarksAtBasePitch() {
    assertEquals(BARK_PITCH, DogVocalization.barkPitch(false));
  }

  @Test
  @DisplayName("a puppy barks at a pitch raised by the puppy multiplier")
  void puppyBarksAtRaisedPitch() {
    assertEquals(BARK_PITCH * PUPPY_BARK_PITCH_MULTIPLIER, DogVocalization.barkPitch(true));
    assertTrue(DogVocalization.barkPitch(true) > DogVocalization.barkPitch(false));
  }

  static Stream<Arguments> barkReadiness() {
    return Stream.of(
        Arguments.of("all clear", true, false, false, 0, true),
        Arguments.of("breed has no bark sound", false, false, false, 0, false),
        Arguments.of("dog is dead", true, true, false, 0, false),
        Arguments.of("dog is asleep in a bed", true, false, true, 0, false),
        Arguments.of("cooldown still running", true, false, false, 1, false),
        Arguments.of("cooldown just elapsed", true, false, false, 0, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("barkReadiness")
  @DisplayName("every bark gate must clear before a bark is allowed")
  void everyBarkGateMustClear(
      final String label,
      final boolean hasBarkSound,
      final boolean dead,
      final boolean sleepingInBed,
      final int barkCooldownTicks,
      final boolean expected) {
    assertEquals(
        expected,
        DogVocalization.isBarkReady(hasBarkSound, dead, sleepingInBed, barkCooldownTicks));
  }

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a breed with no bark sound is never bark-ready, whatever else is true")
  void breedWithoutBarkSoundIsNeverReady(final UnleashedDogBreed breed) {
    assertEquals(
        breed.hasBarkSound(), DogVocalization.isBarkReady(breed.hasBarkSound(), false, false, 0));
  }

  static Stream<Arguments> barkTriggers() {
    final float lowHealth = MAX_HEALTH * LOW_HEALTH_THRESHOLD - 1.0f;
    return Stream.of(
        Arguments.of("nothing of note", false, MAX_HEALTH, false, false, false),
        Arguments.of("player holding a lure", true, MAX_HEALTH, false, false, true),
        Arguments.of("hurt and free to complain", false, lowHealth, false, false, true),
        Arguments.of("hurt but leashed", false, lowHealth, true, false, false),
        Arguments.of("has an attack target", false, MAX_HEALTH, false, true, true),
        Arguments.of(
            "health exactly at the threshold",
            false,
            MAX_HEALTH * LOW_HEALTH_THRESHOLD,
            false,
            false,
            false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("barkTriggers")
  @DisplayName("a bark trigger fires only for a lure, unleashed low health, or a target")
  void barkTriggerFiresOnlyForKnownCauses(
      final String label,
      final boolean playerHoldingLure,
      final float health,
      final boolean leashed,
      final boolean hasTarget,
      final boolean expected) {
    assertEquals(
        expected,
        DogVocalization.hasBarkTrigger(playerHoldingLure, health, MAX_HEALTH, leashed, hasTarget));
  }

  static Stream<Arguments> howlConditions() {
    return Stream.of(
        Arguments.of("full moon night", false, false, false, FULL_MOON_PHASE, true),
        Arguments.of("full moon but daytime", false, false, true, FULL_MOON_PHASE, false),
        Arguments.of("night but wrong moon phase", false, false, false, NEW_MOON_PHASE, false),
        Arguments.of("dead on a full moon night", true, false, false, FULL_MOON_PHASE, false),
        Arguments.of("asleep on a full moon night", false, true, false, FULL_MOON_PHASE, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("howlConditions")
  @DisplayName("howling needs a wakeful living dog under a full moon at night")
  void howlingNeedsFullMoonNight(
      final String label,
      final boolean dead,
      final boolean sleepingInBed,
      final boolean day,
      final int moonPhase,
      final boolean expected) {
    assertEquals(expected, DogVocalization.isHowlConditionMet(dead, sleepingInBed, day, moonPhase));
  }
}
