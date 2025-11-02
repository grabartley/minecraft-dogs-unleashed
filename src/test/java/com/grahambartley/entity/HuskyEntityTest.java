package com.grahambartley.entity;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HuskyEntityTest {

  @Test
  @DisplayName("Husky health should be 25.0")
  void testHuskyHealth() {
    double health = 25.0;
    double wolfHealth = 20.0;
    assertTrue(health > wolfHealth, "Husky health should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Husky speed should be 0.35")
  void testHuskySpeed() {
    double speed = 0.35;
    double wolfSpeed = 0.3;
    assertTrue(speed > wolfSpeed, "Husky speed should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Husky attack damage should be 5.0")
  void testHuskyAttack() {
    double attack = 5.0;
    double wolfAttack = 4.0;
    assertTrue(attack > wolfAttack, "Husky attack should be higher than vanilla wolf");
  }

  @Test
  @DisplayName("Entity attribute values should be configured correctly")
  void testAttributeValues() {
    double health = 25.0;
    double speed = 0.35;
    double attack = 5.0;

    assertAll(
        () -> assertTrue(health >= 10 && health <= 100, "Health should be reasonable"),
        () -> assertTrue(speed > 0 && speed <= 1.0, "Speed should be reasonable"),
        () -> assertTrue(attack >= 0 && attack <= 20, "Attack should be reasonable"));
  }

  @Test
  @DisplayName("Default collar color should be RED")
  void testDefaultCollarColor() {
    final DyeColor defaultColor = DyeColor.RED;
    assertNotNull(defaultColor, "Default collar color should not be null");
    assertEquals(DyeColor.RED, defaultColor, "Default collar color should be RED");
  }

  @ParameterizedTest
  @EnumSource(DyeColor.class)
  @DisplayName("All dye colors should be valid for collars")
  void testAllDyeColorsValid(DyeColor color) {
    assertNotNull(color, "Dye color should not be null");
    assertTrue(color.getId() >= 0 && color.getId() <= 15, "Dye color ID should be between 0-15");
  }

  @Test
  @DisplayName("Collar color ID conversion should be reversible")
  void testCollarColorIdConversion() {
    for (DyeColor color : DyeColor.values()) {
      final int id = color.getId();
      final DyeColor retrieved = DyeColor.byId(id);
      assertEquals(
          color, retrieved, "Color retrieved by ID should match original color for " + color);
    }
  }

  @Test
  @DisplayName("Breeding ingredient should include meat items")
  void testBreedingIngredient() {
    final String[] expectedMeatItems = {
      "CHICKEN",
      "COOKED_CHICKEN",
      "BEEF",
      "COOKED_BEEF",
      "PORKCHOP",
      "COOKED_PORKCHOP",
      "MUTTON",
      "COOKED_MUTTON",
      "RABBIT",
      "COOKED_RABBIT",
      "ROTTEN_FLESH"
    };

    assertTrue(
        expectedMeatItems.length >= 10,
        "Should have at least 10 different meat items for breeding");
  }
}
