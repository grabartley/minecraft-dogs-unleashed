package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnleashedDogEntityTest {

  @Test
  @DisplayName("unleashed dog entity should be a loadable class")
  void unleashedDogEntityShouldLoad() {
    assertNotNull(UnleashedDogEntity.class);
  }
}
