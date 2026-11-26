package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogInteractionsTest {

  static Stream<Arguments> clientConsumeCases() {
    return Stream.of(
        Arguments.of("the owner right-clicking their own dog", true, true, false, true),
        Arguments.of("the owner sneak-right-clicking their own dog", true, true, true, true),
        Arguments.of("a stranger right-clicking an untamed dog", false, false, false, true),
        Arguments.of("a stranger sneak-right-clicking an untamed dog", false, false, true, true),
        Arguments.of("a stranger sneaking to inspect a tamed dog", false, true, true, true),
        Arguments.of("a stranger right-clicking a tamed dog", false, true, false, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("clientConsumeCases")
  @DisplayName("The client only swings the arm when the server has an interaction to run")
  void clientConsumeMatchesServerInteraction(
      final String description,
      final boolean owner,
      final boolean tamed,
      final boolean sneaking,
      final boolean expected) {
    assertEquals(expected, DogInteractions.shouldConsumeOnClient(owner, tamed, sneaking));
  }

  static Stream<Arguments> inspectCases() {
    return Stream.of(
        Arguments.of("a stranger sneaking with an item", false, true, false, true),
        Arguments.of(
            "a stranger sneaking with an empty hand pets instead", false, true, true, false),
        Arguments.of("a stranger clicking without sneaking", false, false, false, false),
        Arguments.of("the owner sneaking with an item", true, true, false, false),
        Arguments.of("the owner sneaking with an empty hand", true, true, true, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("inspectCases")
  @DisplayName("A stranger inspects a dog by sneak clicking it with something in hand")
  void onlyAStrangerSneakingWithAnItemInspects(
      final String description,
      final boolean owner,
      final boolean sneaking,
      final boolean emptyHand,
      final boolean expected) {
    assertEquals(expected, DogInteractions.opensInspect(owner, sneaking, emptyHand));
  }

  static Stream<Arguments> breedingFoodCases() {
    return Stream.of(
        Arguments.of("the owner feeding their adult dog", true, false, true),
        Arguments.of("the owner feeding their puppy", true, true, true),
        Arguments.of("a stranger feeding an adult dog", false, false, false),
        Arguments.of("a stranger feeding a puppy", false, true, true));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("breedingFoodCases")
  @DisplayName("Only the owner can feed breeding food to an adult dog, anyone may feed a puppy")
  void onlyTheOwnerFeedsAnAdultBreedingFood(
      final String description, final boolean owner, final boolean baby, final boolean expected) {
    assertEquals(expected, DogInteractions.mayFeedBreedingItem(owner, baby));
  }
}
