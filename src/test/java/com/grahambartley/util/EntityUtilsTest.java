package com.grahambartley.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntityUtilsTest {

  @Test
  @DisplayName("Valid scale should be between 0.5 and 2.0")
  void testIsValidScale() {
    assertTrue(EntityUtils.isValidScale(0.5f));
    assertTrue(EntityUtils.isValidScale(1.0f));
    assertTrue(EntityUtils.isValidScale(2.0f));
    assertTrue(EntityUtils.isValidScale(1.5f));

    assertFalse(EntityUtils.isValidScale(0.4f));
    assertFalse(EntityUtils.isValidScale(2.1f));
    assertFalse(EntityUtils.isValidScale(0.0f));
    assertFalse(EntityUtils.isValidScale(3.0f));
  }

  @Test
  @DisplayName("Clamp scale should constrain values to valid range")
  void testClampScale() {
    assertEquals(0.5f, EntityUtils.clampScale(0.3f));
    assertEquals(0.5f, EntityUtils.clampScale(0.5f));
    assertEquals(1.0f, EntityUtils.clampScale(1.0f));
    assertEquals(2.0f, EntityUtils.clampScale(2.0f));
    assertEquals(2.0f, EntityUtils.clampScale(3.0f));
    assertEquals(1.5f, EntityUtils.clampScale(1.5f));
  }

  @Test
  @DisplayName("Spawn egg color should combine primary and secondary")
  void testGetSpawnEggColor() {
    int result = EntityUtils.getSpawnEggColor(0xFFFFFF, 0x808080);
    int expected = (0xFFFFFF << 16) | 0x808080;
    assertEquals(expected, result);
  }

  @Test
  @DisplayName("Spawn egg color should reject invalid colors")
  void testGetSpawnEggColorInvalid() {
    assertThrows(
        IllegalArgumentException.class, () -> EntityUtils.getSpawnEggColor(0x1000000, 0x808080));

    assertThrows(IllegalArgumentException.class, () -> EntityUtils.getSpawnEggColor(0xFFFFFF, -1));

    assertThrows(IllegalArgumentException.class, () -> EntityUtils.getSpawnEggColor(-1, 0x808080));
  }

  @Test
  @DisplayName("Mod version should return expected value")
  void testGetModVersion() {
    assertEquals("1.0.0", EntityUtils.getModVersion());
  }
}
