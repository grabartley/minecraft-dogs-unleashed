package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CarryProfileTest {
  @ParameterizedTest
  @ValueSource(
      floats = {0f, -0.1f, -1f, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY})
  @DisplayName("non-positive or non-finite scales are rejected")
  void rejectsInvalidScale(float invalidScale) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(0.0, 0.0, invalidScale));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("non-finite vertical offsets are rejected")
  void rejectsInvalidVerticalOffset(double invalidVertical) {
    assertThrows(
        IllegalArgumentException.class, () -> new CarryProfile(invalidVertical, 0.0, 0.5f));
  }

  @ParameterizedTest
  @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY})
  @DisplayName("non-finite forward offsets are rejected")
  void rejectsInvalidForwardOffset(double invalidForward) {
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(0.0, invalidForward, 0.5f));
  }

  @Test
  @DisplayName("accessors return the constructor values verbatim")
  void exposesAllValues() {
    CarryProfile p = new CarryProfile(0.12, -0.34, 0.56f);
    assertEquals(0.12, p.verticalOffset());
    assertEquals(-0.34, p.forwardOffset());
    assertEquals(0.56f, p.scale());
  }
}
