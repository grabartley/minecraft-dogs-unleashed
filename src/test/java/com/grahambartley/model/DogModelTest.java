package com.grahambartley.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DogModelTest {
  @ParameterizedTest
  @ValueSource(strings = {"HuskyModel", "DachshundModel", "BeagleModel", "ShibaInuModel"})
  @DisplayName("Model class should exist")
  void testModelClassExists(String modelClassName) {
    assertDoesNotThrow(
        () -> {
          Class<?> modelClass = Class.forName("com.grahambartley.model." + modelClassName);
          assertNotNull(modelClass);
        });
  }
}
