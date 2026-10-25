package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PetDetailsScreenTest {

  @ParameterizedTest(name = "screen width {0}")
  @ValueSource(ints = {320, 400, 427, 854, 1920})
  @DisplayName("the portrait column and info column never overlap")
  void portraitAndInfoColumnsStaySeparate(final int screenWidth) {
    final int portraitRight =
        PetDetailsScreen.portraitX(screenWidth) + PetDetailsScreen.PORTRAIT_SIZE;
    final int infoX = PetDetailsScreen.infoColumnX(screenWidth);

    assertTrue(
        infoX > portraitRight,
        "info column at " + infoX + " overlaps portrait ending at " + portraitRight);
  }

  @ParameterizedTest(name = "screen width {0}")
  @ValueSource(ints = {320, 400, 854, 1920})
  @DisplayName("the portrait stays fully on screen")
  void portraitStaysOnScreen(final int screenWidth) {
    final int portraitX = PetDetailsScreen.portraitX(screenWidth);

    assertTrue(portraitX >= 0, "portrait x " + portraitX + " is off screen");
    assertTrue(portraitX + PetDetailsScreen.PORTRAIT_SIZE <= screenWidth);
  }
}
