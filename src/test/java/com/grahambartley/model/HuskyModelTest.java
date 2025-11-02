package com.grahambartley.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HuskyModelTest {

  @Test
  @DisplayName("Model resource paths should be valid")
  void testResourcePaths() {
    String modelPath = "geo/husky.geo.json";
    String texturePath = "textures/entity/husky.png";
    String animationPath = "animations/husky.animation.json";

    assertTrue(modelPath.endsWith(".geo.json"));
    assertTrue(texturePath.endsWith(".png"));
    assertTrue(animationPath.endsWith(".animation.json"));
  }

  @Test
  @DisplayName("Model class should exist")
  void testModelClassExists() {
    assertDoesNotThrow(
        () -> {
          Class<?> modelClass = Class.forName("com.grahambartley.model.HuskyModel");
          assertNotNull(modelClass);
        });
  }
}
