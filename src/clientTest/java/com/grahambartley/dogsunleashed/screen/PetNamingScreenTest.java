package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PetNamingScreenTest {

  static Stream<Arguments> blankInputs() {
    return Stream.of(
        Arguments.of("null", null),
        Arguments.of("empty string", ""),
        Arguments.of("spaces only", "   "),
        Arguments.of("tabs and spaces", " \t "));
  }

  @ParameterizedTest(name = "{0} yields no submittable name")
  @MethodSource("blankInputs")
  @DisplayName("blank-after-trim input resolves to no submittable name so no send fires")
  void blankResolvesToEmpty(final String label, final String rawText) {
    assertFalse(PetNamingScreen.resolveSubmittableName(rawText).isPresent());
  }

  static Stream<Arguments> nonBlankInputs() {
    return Stream.of(
        Arguments.of("plain value", "Rex", "Rex"),
        Arguments.of("leading whitespace stripped", "   Rex", "Rex"),
        Arguments.of("trailing whitespace stripped", "Rex   ", "Rex"),
        Arguments.of("surrounding whitespace stripped", "  Rex  ", "Rex"),
        Arguments.of("interior spaces preserved", "  Good Boy  ", "Good Boy"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("nonBlankInputs")
  @DisplayName("non-blank input resolves to the trimmed name")
  void nonBlankResolvesToTrimmedName(
      final String label, final String rawText, final String expected) {
    final Optional<String> resolved = PetNamingScreen.resolveSubmittableName(rawText);
    assertTrue(resolved.isPresent());
    assertEquals(expected, resolved.get());
  }

  @Test
  @DisplayName("NAME_MAX_LENGTH matches the vanilla-friendly 32 character cap")
  void nameMaxLengthIs32() {
    assertEquals(32, PetNamingScreen.NAME_MAX_LENGTH);
  }
}
