package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogSleepSpotHandoverTest {

  private static final UUID CURRENT = UUID.randomUUID();
  private static final UUID INCOMING = UUID.randomUUID();

  static Stream<Arguments> handovers() {
    return Stream.of(
        Arguments.of("an empty spot displaces nobody", null, INCOMING, Optional.empty()),
        Arguments.of(
            "re-assigning the same dog displaces nobody", CURRENT, CURRENT, Optional.empty()),
        Arguments.of(
            "a new dog displaces the dog already there", CURRENT, INCOMING, Optional.of(CURRENT)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("handovers")
  void displacedIsTheDogThatLosesTheSpot(
      final String description,
      final UUID current,
      final UUID incoming,
      final Optional<UUID> expected) {
    assertEquals(expected, DogSleepSpotHandover.displaced(current, incoming));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("handovers")
  void aSpotIsTakenByAnotherOnlyWhenSomeoneWouldBeDisplaced(
      final String description,
      final UUID current,
      final UUID incoming,
      final Optional<UUID> expected) {
    assertEquals(expected.isPresent(), DogSleepSpotHandover.isTakenByAnother(current, incoming));
  }
}
