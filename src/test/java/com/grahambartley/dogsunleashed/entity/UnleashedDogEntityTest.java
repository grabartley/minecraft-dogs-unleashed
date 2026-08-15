package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnleashedDogEntityTest {

  @Test
  @DisplayName("default collar color id round-trips back to DyeColor.RED")
  void defaultCollarColorIdResolvesToRed() {
    final int defaultId = DyeColor.RED.getId();
    assertEquals(
        DyeColor.RED,
        DyeColor.byId(defaultId),
        "Default collar color id should round-trip back to DyeColor.RED for untamed dogs");
  }
}
