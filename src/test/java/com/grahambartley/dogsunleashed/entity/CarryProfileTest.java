package com.grahambartley.dogsunleashed.entity;

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
    assertThrows(IllegalArgumentException.class, () -> new CarryProfile(invalidScale));
  }

  @Test
  @DisplayName("the scale accessor returns the constructor argument verbatim")
  void scaleIsReturnedVerbatim() {
    assertEquals(0.56f, new CarryProfile(0.56f).scale());
  }
}
