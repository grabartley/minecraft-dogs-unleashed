package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class PetManagerScreenTest {

  private static final int HINT_X = 100;
  private static final int HINT_Y = 200;
  private static final int HINT_WIDTH = 240;
  private static final int HINT_HEIGHT = 9;

  private static final int BUTTON_HEIGHT = 20;

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
  @DisplayName("the keybind hint sits below the summon button and stays on screen")
  void keybindHintDoesNotOverlapTheSummonButton(final int screenHeight) {
    final int summonButtonBottom = PetManagerScreen.summonButtonY(screenHeight) + BUTTON_HEIGHT;
    final int hintY = PetManagerScreen.keybindHintY(screenHeight);

    assertTrue(
        hintY >= summonButtonBottom,
        "hint top " + hintY + " overlaps summon button bottom " + summonButtonBottom);
    assertTrue(
        hintY + HINT_HEIGHT + 1 <= screenHeight,
        "hint underline at "
            + (hintY + HINT_HEIGHT + 1)
            + " falls off a "
            + screenHeight
            + "px screen");
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
