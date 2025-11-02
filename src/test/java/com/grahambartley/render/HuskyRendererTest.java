package com.grahambartley.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyRendererTest {
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
