package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DogPanicPolicyTest {

  static Stream<Arguments> notOnFireCases() {
    return Stream.of(
        Arguments.of(DogCommand.GUARD, false),
        Arguments.of(DogCommand.HUNT, false),
        Arguments.of(DogCommand.FOLLOW, true),
        Arguments.of(DogCommand.HEEL, true),
        Arguments.of(DogCommand.STAY, true),
        Arguments.of(DogCommand.SIT, true),
        Arguments.of(DogCommand.FREE_ROAM, true));
  }

  @ParameterizedTest(name = "{0} flees={1}")
  @MethodSource("notOnFireCases")
  @DisplayName("only Guard and Hunt dogs hold their ground when hurt")
  void guardAndHuntHoldGroundWhenNotOnFire(final DogCommand command, final boolean expectedToFlee) {
    assertEquals(expectedToFlee, DogPanicPolicy.shouldFleeDanger(command, false));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(DogCommand.class)
  @DisplayName("a burning dog flees under every command")
  void burningDogAlwaysFlees(final DogCommand command) {
    assertTrue(DogPanicPolicy.shouldFleeDanger(command, true));
  }
}
