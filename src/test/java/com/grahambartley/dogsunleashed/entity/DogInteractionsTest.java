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
}
