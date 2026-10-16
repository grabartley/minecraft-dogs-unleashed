package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigScreenLayoutTest {

  static Stream<Arguments> screens() {
    return Stream.of(240, 270, 360, 480, 540, 1080)
        .flatMap(height -> Stream.of(Arguments.of(height, false), Arguments.of(height, true)));
  }

  @ParameterizedTest(name = "height {0}, banner {1}")
  @MethodSource("screens")
  @DisplayName("Done and Cancel always sit fully inside the screen")
  void buttonsStayOnScreen(final int height, final boolean showsBanner) {
    final ConfigScreenLayout layout = ConfigScreenLayout.of(height, showsBanner);

    assertTrue(layout.buttonsY() >= 0);
    assertTrue(layout.buttonsY() + ConfigScreenLayout.BUTTON_HEIGHT <= height);
  }

  @ParameterizedTest(name = "height {0}, banner {1}")
  @MethodSource("screens")
  @DisplayName("the list sits between the header text and the buttons")
  void listFitsBetweenHeaderAndButtons(final int height, final boolean showsBanner) {
    final ConfigScreenLayout layout = ConfigScreenLayout.of(height, showsBanner);

    assertTrue(layout.listTop() > ConfigScreenLayout.headerBottom(showsBanner));
    assertTrue(layout.listBottom() <= layout.buttonsY());
    assertTrue(layout.listHeight() > 0);
  }

  static Stream<Arguments> buttonPositions() {
    return Stream.of(Arguments.of(480, 454), Arguments.of(270, 244), Arguments.of(540, 514));
  }

  @ParameterizedTest(name = "height {0} puts the buttons at {1}")
  @MethodSource("buttonPositions")
  @DisplayName("the buttons are centred in a footer pinned to the bottom edge")
  void buttonsArePinnedToTheBottom(final int height, final int expectedButtonsY) {
    assertEquals(expectedButtonsY, ConfigScreenLayout.of(height, false).buttonsY());
  }

  @ParameterizedTest(name = "banner {0}")
  @ValueSource(booleans = {false, true})
  @DisplayName("the list grows with the screen so taller windows show more rows")
  void listGrowsWithScreenHeight(final boolean showsBanner) {
    assertTrue(
        ConfigScreenLayout.of(540, showsBanner).listHeight()
            > ConfigScreenLayout.of(270, showsBanner).listHeight());
  }
}
