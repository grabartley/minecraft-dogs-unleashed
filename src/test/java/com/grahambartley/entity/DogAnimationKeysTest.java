package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogAnimationKeysTest {

  @Test
  @DisplayName("Sleep animation key stays stable for in-bed breed overrides")
  void sleepAnimationKeyMatchesBreedOverrides() {
    assertEquals("sleep", DogAnimationKeys.SLEEP);
  }
}
