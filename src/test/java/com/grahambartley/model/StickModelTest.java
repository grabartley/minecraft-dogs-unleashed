package com.grahambartley.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.model.GeoModel;

class StickModelTest {

  @Test
  @DisplayName("stick model should be a geo model")
  void stickModelShouldBeGeoModel() {
    assertTrue(GeoModel.class.isAssignableFrom(StickModel.class));
  }
}
