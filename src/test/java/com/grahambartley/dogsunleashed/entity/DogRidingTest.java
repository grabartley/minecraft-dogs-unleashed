package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogRidingTest {

  @Test
  @DisplayName("ride speed eases toward its target instead of jumping to it")
  void speedEasesIn() {
    final float first = DogRiding.approach(0.0f, 0.5f);
    assertTrue(first > 0.0f && first < 0.5f);
    float speed = 0.0f;
    for (int tick = 0; tick < 60; tick++) {
      speed = DogRiding.approach(speed, 0.5f);
    }
    assertEquals(0.5f, speed, 0.001f);
  }

  @Test
  @DisplayName("ride speed eases back down when sprinting stops")
  void speedEasesOut() {
    final float slowed = DogRiding.approach(0.6f, 0.36f);
    assertTrue(slowed < 0.6f && slowed > 0.36f);
  }
}
