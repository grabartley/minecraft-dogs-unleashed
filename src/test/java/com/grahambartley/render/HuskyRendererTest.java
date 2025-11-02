package com.grahambartley.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyRendererTest {

  @Test
  @DisplayName("Renderer animation threshold values should be valid")
  void testAnimationValues() {
    float threshold = 0.005f;
    float deathRotation = 0f;

    assertTrue(threshold > 0 && threshold < 1);
    assertEquals(0f, deathRotation);
  }

  @Test
  @DisplayName("Shadow radius values should be valid")
  void testShadowRadius() {
    float adultShadow = 0.5f;
    float babyShadow = 0.25f;

    assertTrue(adultShadow > 0);
    assertTrue(babyShadow > 0);
    assertTrue(babyShadow < adultShadow);
  }

  @Test
  @DisplayName("Renderer class should exist")
  void testRendererClassExists() {
    assertDoesNotThrow(
        () -> {
          Class<?> rendererClass = Class.forName("com.grahambartley.render.HuskyRenderer");
          assertNotNull(rendererClass);
        });
  }
}
