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

  @ParameterizedTest(name = "screen height {0}")
  @ValueSource(ints = {240, 300, 480, 1080})
  @DisplayName("the family preview box keeps space for the action buttons below it")
  void previewBoxLeavesRoomForActions(final int screenHeight) {
    final int boxTop = PetDetailsScreen.previewBoxTop(screenHeight);
    final int boxBottom = screenHeight - 40;
    final int actionsY = screenHeight - 30;

    assertTrue(boxTop < boxBottom, "preview box collapses on a " + screenHeight + "px screen");
    assertTrue(boxBottom <= actionsY, "preview box overlaps the action buttons");
  }
}
