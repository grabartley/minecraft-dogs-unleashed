package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

  @ParameterizedTest(name = "screen height {0} alive {1}")
  @CsvSource({
    "300, true",
    "480, true",
    "1080, true",
    "300, false",
    "480, false",
    "1080, false",
  })
  @DisplayName("the family preview box keeps space for the action buttons below it")
  void previewBoxLeavesRoomForActions(final int screenHeight, final boolean alive) {
    final int boxTop = PetDetailsScreen.previewBoxTop(screenHeight, alive);
    final int boxBottom = screenHeight - 40;
    final int actionsY = screenHeight - 30;

    assertTrue(boxTop < boxBottom, "preview box collapses on a " + screenHeight + "px screen");
    assertTrue(boxBottom <= actionsY, "preview box overlaps the action buttons");
  }

  @ParameterizedTest(name = "screen height {0} alive {1}")
  @CsvSource({
    "240, true",
    "480, true",
    "1080, true",
    "240, false",
    "1080, false",
  })
  @DisplayName("the family preview box never overlaps the stats section")
  void previewBoxNeverOverlapsStats(final int screenHeight, final boolean alive) {
    final int boxTop = PetDetailsScreen.previewBoxTop(screenHeight, alive);

    assertTrue(
        boxTop > PetDetailsScreen.infoBottom(alive),
        "preview box top " + boxTop + " overlaps the info column");
  }

  @ParameterizedTest(name = "screen height {0}")
  @ValueSource(ints = {480, 540, 1080})
  @DisplayName("on tall screens the preview box caps its height to leave room for the info column")
  void previewBoxCapsHeightOnTallScreens(final int screenHeight) {
    final int boxTop = PetDetailsScreen.previewBoxTop(screenHeight, true);
    final int boxHeight = screenHeight - 40 - boxTop;

    assertTrue(
        boxHeight <= PetDetailsScreen.FAMILY_PREVIEW_HEIGHT,
        "preview box height " + boxHeight + " exceeds the cap");
  }

  @ParameterizedTest(name = "screen height {0} shows preview {1}")
  @CsvSource({
    "240, false",
    "300, true",
    "480, true",
    "1080, true",
  })
  @DisplayName("the family preview hides instead of overlapping on short screens")
  void previewHidesOnShortScreens(final int screenHeight, final boolean expected) {
    assertEquals(expected, PetDetailsScreen.shouldShowFamilyPreview(screenHeight, true));
  }
}
