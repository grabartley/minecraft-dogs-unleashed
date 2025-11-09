package com.grahambartley;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DogsUnleashedTest {

  @Test
  @DisplayName("Mod ID should be dogs-unleashed")
  void testModId() {
    assertEquals("dogs-unleashed", DogsUnleashed.MOD_ID);
  }

  @Test
  @DisplayName("Mod ID should follow Minecraft conventions")
  void testModIdFormat() {
    String modId = DogsUnleashed.MOD_ID;

    assertFalse(modId.contains(" "), "Mod ID should not contain spaces");
    assertFalse(modId.contains("_"), "Mod ID should use hyphens not underscores");
    assertTrue(modId.matches("[a-z0-9-]+"), "Mod ID should be lowercase alphanumeric with hyphens");
  }

  @Test
  @DisplayName("Logger should have correct name")
  void testLoggerName() {
    assertEquals(DogsUnleashed.MOD_ID, DogsUnleashed.log.getName());
  }
}
