package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PettingRulesTest {

  private static final long NOW = 48_000L;
  private static final Path ANIMATIONS =
      Path.of("src/client/resources/assets/dogs-unleashed/animations");
  private static final int TICKS_PER_SECOND = 20;

  @ParameterizedTest(name = "sneaking {0}, main hand {1}, empty hand {2}, steady {3} -> pat {4}")
  @CsvSource({
    "true, true, true, true, true",
    "false, true, true, true, false",
    "true, false, true, true, false",
    "true, true, false, true, false",
    "true, true, true, false, false",
    "false, false, false, false, false",
    "true, false, false, true, false"
  })
  @DisplayName("only a sneak click with an empty main hand, standing on the ground, is a pat")
  void onlyASteadySneakClickWithAnEmptyMainHandIsAPat(
      final boolean sneaking,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean steady,
      final boolean expected) {
    assertEquals(expected, PettingRules.isPetGesture(sneaking, mainHand, emptyHand, steady));
  }

  static Stream<Arguments> dogStates() {
    return Stream.of(
        Arguments.of("a calm, free dog", state(-1), true),
        Arguments.of("a sleeping dog", state(0), false),
        Arguments.of("a dog in a fight", state(1), false),
        Arguments.of("a dog angry at the petter", state(2), false),
        Arguments.of("a dog on an errand, a meal or a dig", state(3), false),
        Arguments.of("a dog doing a trick", state(4), false),
        Arguments.of("a dog in a vehicle", state(5), false),
        Arguments.of("a dog carrying a rider", state(6), false),
        Arguments.of("a dog hitched to a sled", state(7), false),
        Arguments.of("a howling dog", state(8), false),
        Arguments.of("a dog shaking off", state(9), false),
        Arguments.of("a growling dog", state(10), false));
  }

  private static PettingRules.DogState state(final int setFact) {
    final boolean[] facts = new boolean[11];
    if (setFact >= 0) {
      facts[setFact] = true;
    }
    return new PettingRules.DogState(
        facts[0], facts[1], facts[2], facts[3], facts[4], facts[5], facts[6], facts[7], facts[8],
        facts[9], facts[10]);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("dogStates")
  @DisplayName("a dog takes a pat only when it is calm and free")
  void aDogTakesAPatOnlyWhenCalmAndFree(
      final String label, final PettingRules.DogState state, final boolean expected) {
    assertEquals(
        expected, PettingRules.welcomes(state, NOW, PettingRules.NEVER, PettingRules.NEVER));
  }

  @ParameterizedTest(name = "last pat {0} ticks ago -> cooled down {1}")
  @CsvSource({"0, false", "1, false", "29, false", "30, true", "31, true", "6000, true"})
  @DisplayName("a pat that arrives while another plays is ignored until it ends")
  void aPatArrivingMidPatIsIgnored(final long ticksAgo, final boolean expected) {
    assertEquals(expected, PettingRules.cooledDown(NOW, NOW - ticksAgo));
    assertEquals(!expected, PettingRules.isBeingPetted(NOW, NOW - ticksAgo));
    assertEquals(
        expected, PettingRules.welcomes(state(-1), NOW, NOW - ticksAgo, PettingRules.NEVER));
  }

  @ParameterizedTest(name = "last pat stored at {0}")
  @CsvSource({"-9223372036854775808", "48001", "96000"})
  @DisplayName("a dog never petted, or petted in a later clock, takes a pat now")
  void aDogNeverPettedOrFromALaterClockTakesAPat(final long lastPettedAt) {
    assertEquals(true, PettingRules.cooledDown(NOW, lastPettedAt));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = "CROSS_BREED",
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("the cooldown lasts exactly as long as each rig's petted clip")
  void theCooldownLastsAsLongAsEachRigsPettedClip(final UnleashedDogBreed breed)
      throws IOException {
    try (Reader reader =
        Files.newBufferedReader(
            ANIMATIONS.resolve(breed.serializedId() + ".animation.json"), StandardCharsets.UTF_8)) {
      final double seconds =
          JsonParser.parseReader(reader)
              .getAsJsonObject()
              .getAsJsonObject("animations")
              .getAsJsonObject(DogAnimationKeys.PETTED)
              .get("animation_length")
              .getAsDouble();
      assertEquals(Math.round(seconds * TICKS_PER_SECOND), PettingRules.COOLDOWN_TICKS);
    }
  }

  @ParameterizedTest(name = "asked {0} ticks ago -> still walking over {1}")
  @CsvSource({"0, true", "67, true", "68, false", "-5, false"})
  @DisplayName("a dog walks over to the petter for a short while, then gives up")
  void theWalkOverEnds(final long ticksAgo, final boolean expected) {
    assertEquals(expected, PettingRules.isApproaching(NOW, NOW - ticksAgo));
    assertEquals(
        !expected, PettingRules.welcomes(state(-1), NOW, PettingRules.NEVER, NOW - ticksAgo));
  }

  @ParameterizedTest(name = "a dog walking over to someone takes no other pat")
  @CsvSource({"-9223372036854775808, false"})
  @DisplayName("a dog never asked is not walking over")
  void aDogNeverAskedIsNotWalking(final long requestedAt, final boolean expected) {
    assertEquals(expected, PettingRules.isApproaching(NOW, requestedAt));
  }

  @ParameterizedTest(name = "asked {0} ticks ago -> knelt {1}")
  @CsvSource({"0, false", "11, false", "12, true", "40, true"})
  @DisplayName("the pat waits for the player to finish kneeling")
  void thePatWaitsForTheKneel(final long ticksAgo, final boolean expected) {
    assertEquals(expected, PettingRules.hasSettled(NOW, NOW - ticksAgo));
  }

  @ParameterizedTest(name = "moved {0} blocks -> walked away {1}")
  @CsvSource({"0, false", "0.4, false", "1.0, false", "1.01, true", "5, true"})
  @DisplayName("a petter who walks off calls the pat off")
  void aPetterWhoWalksOffCallsItOff(final double moved, final boolean expected) {
    assertEquals(expected, PettingRules.walkedAway(moved));
  }

  @ParameterizedTest(name = "yaw {0}, dog at {1},{2} -> turned away {3}")
  @CsvSource({
    "0, 0, 1, false",
    "0, 1, 1, false",
    "0, 1, 0, false",
    "0, 0, -1, true",
    "90, -1, 0, false",
    "90, 1, 0, true",
    "-170, 0.1, -1, false",
    "350, 0, 1, false"
  })
  @DisplayName("a petter who turns their back on the dog calls the pat off")
  void turningAwayCallsItOff(
      final float yaw, final double dx, final double dz, final boolean expected) {
    assertEquals(expected, PettingRules.turnedAway(yaw, dx, dz));
  }

  @ParameterizedTest(name = "{0} blocks from the spot -> step straight in {1}")
  @CsvSource({"0.2, true", "1.5, true", "1.6, false", "6, false"})
  @DisplayName("the last stretch to the hand is a straight step, not a path")
  void theLastStretchIsAStraightStep(final double blocks, final boolean expected) {
    assertEquals(expected, PettingRules.closeEnoughToStep(blocks));
  }

  @ParameterizedTest(name = "body {0} toward {1} -> {2}")
  @CsvSource({
    "0, 0, 0",
    "0, 15, 15",
    "0, 90, 20",
    "0, -90, -20",
    "170, -170, 190",
    "-170, 170, -190"
  })
  @DisplayName("a dog at its spot turns into place a step at a time, the short way round")
  void aDogTurnsIntoPlace(final float body, final float spot, final float expected) {
    assertEquals(expected, PettingRules.turnedInToward(body, spot), 1.0e-4f);
  }

  @ParameterizedTest(name = "body {0}, spot {1} -> facing {2}")
  @CsvSource({
    "0, 0, true",
    "0, 15, true",
    "0, 16, false",
    "54, 123, false",
    "350, -5, true",
    "180, -175, true"
  })
  @DisplayName("the pat waits until the dog faces along its spot")
  void thePatWaitsUntilTheDogFacesAlong(final float body, final float spot, final boolean facing) {
    assertEquals(facing, PettingRules.facesSpot(body, spot));
  }
}
