package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CarryProfileTest {

  @ParameterizedTest(name = "verticalOffset = {0}")
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("constructor rejects non-finite vertical offsets")
  void rejectsInvalidVerticalOffset(final double invalidVertical) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(invalidVertical, 0.0));
  }

  @ParameterizedTest(name = "forwardOffset = {0}")
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("constructor rejects non-finite forward offsets")
  void rejectsInvalidForwardOffset(final double invalidForward) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(0.0, invalidForward));
  }

  @Test
  @DisplayName("accessors return the constructor arguments verbatim")
  void accessorsReturnConstructorArguments() {
    final CarryProfile profile = new CarryProfile(0.12, -0.34);
    assertEquals(0.12, profile.verticalOffset());
    assertEquals(-0.34, profile.forwardOffset());
  }
}
