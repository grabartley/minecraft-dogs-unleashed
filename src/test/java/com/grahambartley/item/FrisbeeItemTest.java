package com.grahambartley.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrisbeeItemTest {

  @Test
  @DisplayName(
      "color name formatting should capitalize the first letter and replace underscores with spaces")
  void colorNameFormattingShouldCapitalizeAndReplaceUnderscores() {
    for (DyeColor color : DyeColor.values()) {
      String name = color.getName();
      String formatted = name.substring(0, 1).toUpperCase() + name.substring(1).replace('_', ' ');
      assertFalse(formatted.isEmpty(), "Formatted name for " + name + " should not be empty");
      assertFalse(
          formatted.contains("_"), "Formatted name for " + name + " should have no underscores");
      assertTrue(
          Character.isUpperCase(formatted.charAt(0)),
          "Formatted name for " + name + " should start with an uppercase letter");
    }
  }
}
