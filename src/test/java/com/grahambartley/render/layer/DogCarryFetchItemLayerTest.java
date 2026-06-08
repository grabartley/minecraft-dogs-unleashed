package com.grahambartley.render.layer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogCarryFetchItemLayerTest {

  @Test
  @DisplayName("dog carry fetch item layer should load without errors")
  void dogCarryFetchItemLayerShouldLoad() {
    assertNotNull(DogCarryFetchItemLayer.class);
  }
}
