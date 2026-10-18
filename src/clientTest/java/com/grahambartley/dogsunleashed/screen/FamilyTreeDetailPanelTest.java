package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamilyTreeDetailPanelTest {

  @ParameterizedTest(name = "{0} of {1} health is {2}")
  @CsvSource({
    "20, 20, 0x55FF55",
    "10.5, 20, 0x55FF55",
    "10, 20, 0xFF5555",
    "1, 20, 0xFF5555",
    "0, 0, 0xFF5555"
  })
  @DisplayName("health reads green above half and red at or below it")
  void healthColorSplitsAtHalf(final float health, final float maxHealth, final String expected) {
    assertEquals(
        Integer.parseInt(expected.substring(2), 16),
        FamilyTreeDetailPanel.healthColor(health, maxHealth));
  }
}
