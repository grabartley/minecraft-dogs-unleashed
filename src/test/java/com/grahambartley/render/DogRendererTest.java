package com.grahambartley.render;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogRendererTest {
  @ParameterizedTest
  @ValueSource(strings = {"HuskyRenderer", "DachshundRenderer"})
  @DisplayName("Renderer class should exist")
  void testRendererClassExists(String rendererClassName) {
    assertDoesNotThrow(
        () -> {
          Class<?> rendererClass = Class.forName("com.grahambartley.render." + rendererClassName);
          assertNotNull(rendererClass);
        });
  }
}
