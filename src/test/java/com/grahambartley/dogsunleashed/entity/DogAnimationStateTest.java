package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogAnimationStateTest {

  @Test
  @DisplayName("bones ease back over GeckoLib's default single tick until told otherwise")
  void startsAtOneTick() {
    assertEquals(1.0, new DogAnimationState(null).boneResetTicks());
  }

  static Stream<Arguments> resets() {
    return Stream.of(
        Arguments.of("a hand-back stretches the reset", List.of(5.0), 5.0),
        Arguments.of("the reset goes back to one tick afterwards", List.of(5.0, 1.0), 1.0),
        Arguments.of("the latest value wins", List.of(1.0, 5.0, 3.0), 3.0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("resets")
  @DisplayName("the bone reset time is the last one set")
  void keepsLastReset(final String scenario, final List<Double> set, final double expected) {
    final DogAnimationState state = new DogAnimationState(null);
    set.forEach(state::setBoneResetTicks);
    assertEquals(expected, state.boneResetTicks());
  }
}
