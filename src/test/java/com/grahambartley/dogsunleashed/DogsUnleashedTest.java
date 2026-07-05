package com.grahambartley.dogsunleashed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogsUnleashedTest {

  @Test
  @DisplayName("Mod ID is the canonical dogs-unleashed identifier")
  void modIdMatchesCanonicalValue() {
    assertEquals("dogs-unleashed", DogsUnleashed.MOD_ID);
  }

  @Test
  @DisplayName("Logger is named after the mod id")
  void loggerNameMatchesModId() {
    assertEquals(DogsUnleashed.MOD_ID, DogsUnleashed.log.getName());
  }
}
