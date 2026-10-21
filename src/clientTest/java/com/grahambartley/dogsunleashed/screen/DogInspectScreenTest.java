package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.network.payload.WildTrustView;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class DogInspectScreenTest {

  @ParameterizedTest(name = "screen {0}x{1}")
  @CsvSource({
    "427, 240",
    "854, 480",
    "960, 540",
    "1920, 1080",
  })
  @DisplayName("the card stays fully on screen at common gui sizes, with or without a trust row")
  void cardStaysOnScreen(final int screenWidth, final int screenHeight) {
    for (final WildTrustView trust :
        new WildTrustView[] {WildTrustView.NONE, WildTrustView.building(30)}) {
      for (final boolean personality : new boolean[] {false, true}) {
        final int height = DogInspectScreen.cardHeight(trust, true, personality);
        final int left = DogInspectScreen.cardLeft(screenWidth);
        final int top = DogInspectScreen.cardTop(screenHeight, height);

        assertTrue(left >= 0, "card left " + left + " is off screen");
        assertTrue(top >= 0, "card top " + top + " is off screen");
        assertTrue(left + DogInspectScreen.CARD_WIDTH <= screenWidth);
        assertTrue(top + height <= screenHeight);
      }
    }
  }

  static Stream<Arguments> cardHeightCases() {
    final int grown = DogInspectScreen.CARD_HEIGHT + DogInspectScreen.TRUST_ROW_HEIGHT;
    final int personality = DogInspectScreen.PERSONALITY_ROW_HEIGHT;
    return Stream.of(
        Arguments.of(WildTrustView.NONE, false, false, DogInspectScreen.CARD_HEIGHT),
        Arguments.of(WildTrustView.NONE, true, false, grown),
        Arguments.of(WildTrustView.TOO_WILD, false, false, grown),
        Arguments.of(WildTrustView.SHUNNED, false, false, grown),
        Arguments.of(WildTrustView.building(10), false, false, grown),
        Arguments.of(WildTrustView.NONE, false, true, DogInspectScreen.CARD_HEIGHT + personality),
        Arguments.of(WildTrustView.NONE, true, true, grown + personality));
  }

  @ParameterizedTest(name = "trust {0}, tamed {1}, personality {2} -> {3}")
  @MethodSource("cardHeightCases")
  @DisplayName("the card grows by one row each for trust or bond and for personality")
  void cardGrowsForTrustBondAndPersonality(
      final WildTrustView trust,
      final boolean tamed,
      final boolean personality,
      final int expected) {
    assertEquals(expected, DogInspectScreen.cardHeight(trust, tamed, personality));
  }

  @ParameterizedTest(name = "screen width {0}")
  @ValueSource(ints = {427, 854, 1920})
  @DisplayName("the card is horizontally centered")
  void cardIsHorizontallyCentered(final int screenWidth) {
    final int left = DogInspectScreen.cardLeft(screenWidth);
    final int right = screenWidth - left - DogInspectScreen.CARD_WIDTH;

    assertTrue(Math.abs(left - right) <= 1, "card is off center: " + left + " vs " + right);
  }

  @Test
  @DisplayName("the portrait and info rows fit inside the card")
  void contentFitsInsideCard() {
    final int contentBottom =
        DogInspectScreen.CARD_PADDING
            + DogInspectScreen.PORTRAIT_SIZE
            + 8
            + 4 * 14
            + DogStatsPanel.totalHeight();

    assertTrue(
        contentBottom <= DogInspectScreen.CARD_HEIGHT - DogInspectScreen.CARD_PADDING,
        "content bottom " + contentBottom + " overflows the card");
    assertTrue(
        contentBottom + DogInspectScreen.TRUST_ROW_HEIGHT + DogInspectScreen.PERSONALITY_ROW_HEIGHT
            <= DogInspectScreen.cardHeight(WildTrustView.building(1), false, true)
                - DogInspectScreen.CARD_PADDING,
        "the trust row overflows the card");
  }
}
