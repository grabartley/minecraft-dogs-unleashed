package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PetManagerScreenTest {

  private static final int HINT_X = 100;
  private static final int HINT_Y = 200;
  private static final int HINT_WIDTH = 240;
  private static final int HINT_HEIGHT = 9;

  private static final int ROW_X = 40;
  private static final int ROW_Y = 95;
  private static final int ENTRY_WIDTH = 280;
  private static final int ENTRY_VISIBLE_HEIGHT = 55;

  private static boolean hintHit(final double mouseX, final double mouseY) {
    return PetManagerScreen.isWithinKeybindHint(
        mouseX, mouseY, HINT_X, HINT_Y, HINT_WIDTH, HINT_HEIGHT);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pointsOnTheHint")
  @DisplayName("clicks landing on the keybind hint text are inside its link area")
  void pointsOnTheHintAreInside(final String label, final double mouseX, final double mouseY) {
    assertTrue(hintHit(mouseX, mouseY));
  }

  static Stream<Arguments> pointsOnTheHint() {
    return Stream.of(
        Arguments.of("hint center", HINT_X + HINT_WIDTH / 2.0, HINT_Y + HINT_HEIGHT / 2.0),
        Arguments.of("top-left corner", (double) HINT_X, (double) HINT_Y),
        Arguments.of(
            "just inside the bottom-right corner",
            HINT_X + HINT_WIDTH - 0.5,
            HINT_Y + HINT_HEIGHT - 0.5),
        Arguments.of("first pixel column", HINT_X + 0.5, HINT_Y + 4.0),
        Arguments.of("last pixel row", HINT_X + 10.0, HINT_Y + HINT_HEIGHT - 1.0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pointsOffTheHint")
  @DisplayName("clicks outside the keybind hint text do not open the controls screen")
  void pointsOffTheHintAreOutside(final String label, final double mouseX, final double mouseY) {
    assertFalse(hintHit(mouseX, mouseY));
  }

  static Stream<Arguments> pointsOffTheHint() {
    return Stream.of(
        Arguments.of("one pixel left of the hint", HINT_X - 1.0, HINT_Y + 4.0),
        Arguments.of("at the right edge", (double) (HINT_X + HINT_WIDTH), HINT_Y + 4.0),
        Arguments.of("one pixel above the hint", HINT_X + 10.0, HINT_Y - 1.0),
        Arguments.of("at the bottom edge", HINT_X + 10.0, (double) (HINT_Y + HINT_HEIGHT)),
        Arguments.of("over the pet list far above", HINT_X + 10.0, 95.0),
        Arguments.of("off screen", -50.0, -50.0));
  }

  @ParameterizedTest(name = "screen height {0}")
  @ValueSource(ints = {240, 260, 300, 480, 720})
  @DisplayName("the keybind hint stays fully on screen")
  void keybindHintStaysOnScreen(final int screenHeight) {
    final int hintY = PetManagerScreen.keybindHintY(screenHeight);

    assertTrue(
        hintY + HINT_HEIGHT + 1 <= screenHeight,
        "hint underline at "
            + (hintY + HINT_HEIGHT + 1)
            + " falls off a "
            + screenHeight
            + "px screen");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pointsOnTheRowSummonButton")
  @DisplayName("clicks landing on a row's summon button are inside its hit area")
  void pointsOnTheRowSummonButtonAreInside(
      final String label, final double mouseX, final double mouseY) {
    assertTrue(PetManagerScreen.isWithinRowSummonButton(mouseX, mouseY, ROW_X, ROW_Y));
  }

  static Stream<Arguments> pointsOnTheRowSummonButton() {
    final int buttonX = PetManagerScreen.rowSummonButtonX(ROW_X);
    final int buttonY = PetManagerScreen.rowSummonButtonY(ROW_Y);
    return Stream.of(
        Arguments.of(
            "button center",
            buttonX + PetManagerScreen.ROW_SUMMON_WIDTH / 2.0,
            buttonY + PetManagerScreen.ROW_SUMMON_HEIGHT / 2.0),
        Arguments.of("top-left corner", (double) buttonX, (double) buttonY),
        Arguments.of(
            "just inside the bottom-right corner",
            buttonX + PetManagerScreen.ROW_SUMMON_WIDTH - 0.5,
            buttonY + PetManagerScreen.ROW_SUMMON_HEIGHT - 0.5));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("pointsOffTheRowSummonButton")
  @DisplayName("clicks elsewhere on the row miss the summon button and open the details screen")
  void pointsOffTheRowSummonButtonAreOutside(
      final String label, final double mouseX, final double mouseY) {
    assertFalse(PetManagerScreen.isWithinRowSummonButton(mouseX, mouseY, ROW_X, ROW_Y));
  }

  static Stream<Arguments> pointsOffTheRowSummonButton() {
    final int buttonX = PetManagerScreen.rowSummonButtonX(ROW_X);
    final int buttonY = PetManagerScreen.rowSummonButtonY(ROW_Y);
    return Stream.of(
        Arguments.of("over the portrait", ROW_X + 10.0, ROW_Y + 10.0),
        Arguments.of("over the name text", ROW_X + 70.0, ROW_Y + 8.0),
        Arguments.of("one pixel left of the button", buttonX - 1.0, buttonY + 5.0),
        Arguments.of(
            "at the button's right edge",
            (double) (buttonX + PetManagerScreen.ROW_SUMMON_WIDTH),
            buttonY + 5.0),
        Arguments.of("one pixel above the button", buttonX + 5.0, buttonY - 1.0),
        Arguments.of(
            "below the button",
            buttonX + 5.0,
            (double) (buttonY + PetManagerScreen.ROW_SUMMON_HEIGHT)));
  }

  @Test
  @DisplayName("the row summon button sits fully inside the visible row")
  void rowSummonButtonFitsInsideTheRow() {
    final int buttonX = PetManagerScreen.rowSummonButtonX(ROW_X);
    final int buttonY = PetManagerScreen.rowSummonButtonY(ROW_Y);

    assertTrue(buttonX > ROW_X, "button starts inside the row");
    assertTrue(
        buttonX + PetManagerScreen.ROW_SUMMON_WIDTH <= ROW_X + ENTRY_WIDTH,
        "button ends inside the row");
    assertTrue(buttonY > ROW_Y, "button top inside the row");
    assertTrue(
        buttonY + PetManagerScreen.ROW_SUMMON_HEIGHT <= ROW_Y + ENTRY_VISIBLE_HEIGHT,
        "button bottom inside the visible row");
  }

  @ParameterizedTest(name = "screen height {0}")
  @ValueSource(ints = {240, 300, 720})
  @DisplayName("the keybind hint is anchored to the bottom of the screen")
  void keybindHintIsBottomAnchored(final int screenHeight) {
    assertEquals(
        screenHeight - PetManagerScreen.keybindHintY(screenHeight),
        240 - PetManagerScreen.keybindHintY(240));
  }
}
