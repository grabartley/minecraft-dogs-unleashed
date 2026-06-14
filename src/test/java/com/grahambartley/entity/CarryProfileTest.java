package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CarryProfileTest {

  @ParameterizedTest(name = "scale = {0}")
  @ValueSource(
      floats = {0f, -0.1f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
  @DisplayName("constructor rejects non-positive or non-finite scales")
  void rejectsInvalidScale(final float invalidScale) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(0.0, 0.0, invalidScale));
  }

  @ParameterizedTest(name = "verticalOffset = {0}")
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("constructor rejects non-finite vertical offsets")
  void rejectsInvalidVerticalOffset(final double invalidVertical) {
    assertThrows(
        IllegalArgumentException.class, () -> new CarryProfile(invalidVertical, 0.0, 0.5f));
  }

  @ParameterizedTest(name = "forwardOffset = {0}")
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("constructor rejects non-finite forward offsets")
  void rejectsInvalidForwardOffset(final double invalidForward) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(0.0, invalidForward, 0.5f));
  }

  @Test
  @DisplayName("accessors return the constructor arguments verbatim")
  void accessorsReturnConstructorArguments() {
    final CarryProfile profile = new CarryProfile(0.12, -0.34, 0.56f);
    assertEquals(0.12, profile.verticalOffset());
    assertEquals(-0.34, profile.forwardOffset());
    assertEquals(0.56f, profile.scale());
  }
}
