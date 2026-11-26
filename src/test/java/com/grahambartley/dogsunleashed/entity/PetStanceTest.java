package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PetStanceTest {

  @ParameterizedTest(name = "head top {0} blocks -> kneel {1}")
  @CsvSource({"0.3, 1", "0.95, 1", "1.05, 0.5", "1.15, 0", "1.5, 0", "-1, 1"})
  @DisplayName(
      "a player kneels for a dog smaller than them, stands for a big one, and blends between")
  void kneelsForSmallDogsOnly(final float headTop, final float kneel) {
    assertEquals(kneel, PetStance.kneelWeight(headTop), 1.0e-4f);
  }
}
