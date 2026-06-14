package com.grahambartley;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModConstantsTest {

  static Stream<Arguments> tunableConstants() {
    return Stream.of(
        Arguments.of("MINECRAFT_TICK_RATE", ModConstants.MINECRAFT_TICK_RATE, 20),
        Arguments.of("BARK_COOLDOWN_TICKS", ModConstants.BARK_COOLDOWN_TICKS, 6 * 20),
        Arguments.of("RANDOM_BARK_CHANCE", ModConstants.RANDOM_BARK_CHANCE, 7200),
        Arguments.of("HOWL_COOLDOWN_TICKS", ModConstants.HOWL_COOLDOWN_TICKS, 30 * 20),
        Arguments.of("RANDOM_HOWL_CHANCE", ModConstants.RANDOM_HOWL_CHANCE, 4000),
        Arguments.of("HOWL_DURATION_TICKS", ModConstants.HOWL_DURATION_TICKS, (int) (4.5f * 20)),
        Arguments.of("FULL_MOON_PHASE", ModConstants.FULL_MOON_PHASE, 0));
  }

  @ParameterizedTest(name = "{0} = {2}")
  @MethodSource("tunableConstants")
  @DisplayName("tunable integer constants hold their documented values")
  void tunableConstantsHoldDocumentedValues(
      final String name, final int actual, final int expected) {
    assertEquals(expected, actual, name);
  }

  @ParameterizedTest(name = "{0} = {2}")
  @MethodSource("floatConstants")
  @DisplayName("tunable float constants hold their documented values")
  void tunableFloatConstantsHoldDocumentedValues(
      final String name, final float actual, final float expected) {
    assertEquals(expected, actual, 0.0f, name);
  }

  static Stream<Arguments> floatConstants() {
    return Stream.of(
        Arguments.of("LOW_HEALTH_THRESHOLD", ModConstants.LOW_HEALTH_THRESHOLD, 0.3f),
        Arguments.of("BARK_PITCH", ModConstants.BARK_PITCH, 1.0f),
        Arguments.of("HOWL_PITCH", ModConstants.HOWL_PITCH, 1.0f));
  }
}
