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

  @ParameterizedTest(name = "sneaking {0}, main hand {1}, empty hand {2} -> pat {3}")
  @CsvSource({
    "true, true, true, true",
    "false, true, true, false",
    "true, false, true, false",
    "true, true, false, false",
    "false, false, false, false",
    "true, false, false, false"
  })
  @DisplayName("only a sneak click with an empty main hand is a pat")
  void onlyASneakClickWithAnEmptyMainHandIsAPat(
      final boolean sneaking,
      final boolean mainHand,
      final boolean emptyHand,
      final boolean expected) {
    assertEquals(expected, PettingRules.isPetGesture(sneaking, mainHand, emptyHand));
  }

  static Stream<Arguments> dogStates() {
    return Stream.of(
        Arguments.of("a calm, free dog", state(-1), true),
        Arguments.of("a sleeping dog", state(0), false),
        Arguments.of("a dog targeting the petter", state(1), false),
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

  @ParameterizedTest(name = "{0} blocks apart, half width {1} -> in reach {2}")
  @CsvSource({
    "0.5, 0.4, true",
    "0.85, 0.4, true",
    "0.86, 0.4, false",
    "0.6, 0.2, true",
    "0.66, 0.2, false",
    "3.0, 0.4, false"
  })
  @DisplayName("a dog close enough to the hand is petted where it stands")
  void aDogCloseEnoughIsInReach(
      final double distance, final double halfWidth, final boolean expected) {
    assertEquals(expected, PettingRules.inReach(distance, halfWidth));
  }

  @ParameterizedTest(name = "asked {0} ticks ago -> knelt {1}")
  @CsvSource({"0, false", "7, false", "8, true", "40, true"})
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

  @ParameterizedTest(name = "yaw {0}, right handed {1} -> {2}, {3}")
  @CsvSource({
    "0, true, -0.2, 0.68",
    "0, false, 0.2, 0.68",
    "180, true, 0.2, -0.68",
    "90, true, -0.68, -0.2",
    "-90, false, 0.68, -0.2"
  })
  @DisplayName("the dog comes to a spot in front of the player, beside the patting hand")
  void theDogComesToTheHandSide(
      final float yaw, final boolean rightHanded, final double x, final double z) {
    final PettingRules.Spot spot = PettingRules.handSpot(0.0, 0.0, yaw, rightHanded, 0.4);
    assertEquals(x, spot.x(), 1.0e-6);
    assertEquals(z, spot.z(), 1.0e-6);
  }

  @ParameterizedTest(name = "half width {0}, yaw {1}")
  @CsvSource({"0.2, 0", "0.4, 45", "0.4, -120", "0.6, 200", "0.9, 10"})
  @DisplayName("a dog standing on its hand spot is in reach of the hand")
  void theHandSpotIsInReach(final double halfWidth, final float yaw) {
    final PettingRules.Spot spot = PettingRules.handSpot(0.0, 0.0, yaw, true, halfWidth);
    assertEquals(
        true, PettingRules.inReach(Math.hypot(spot.x(), spot.z()), halfWidth), "spot " + spot);
  }

  @ParameterizedTest(name = "{0} blocks from the spot -> step straight in {1}")
  @CsvSource({"0.2, true", "1.5, true", "1.6, false", "6, false"})
  @DisplayName("the last stretch to the hand is a straight step, not a path")
  void theLastStretchIsAStraightStep(final double blocks, final boolean expected) {
    assertEquals(expected, PettingRules.closeEnoughToStep(blocks));
  }
}
