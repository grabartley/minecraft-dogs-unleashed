package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.network.payload.WildTrustView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
      final int height = DogInspectScreen.cardHeight(trust);
      final int left = DogInspectScreen.cardLeft(screenWidth);
      final int top = DogInspectScreen.cardTop(screenHeight, height);

      assertTrue(left >= 0, "card left " + left + " is off screen");
      assertTrue(top >= 0, "card top " + top + " is off screen");
      assertTrue(left + DogInspectScreen.CARD_WIDTH <= screenWidth);
      assertTrue(top + height <= screenHeight);
    }
  }

  @Test
  @DisplayName("the card grows by one row only when it shows trust")
  void cardGrowsForTrust() {
    assertEquals(DogInspectScreen.CARD_HEIGHT, DogInspectScreen.cardHeight(WildTrustView.NONE));
    assertEquals(
        DogInspectScreen.CARD_HEIGHT + DogInspectScreen.TRUST_ROW_HEIGHT,
        DogInspectScreen.cardHeight(WildTrustView.TOO_WILD));
    assertEquals(
        DogInspectScreen.CARD_HEIGHT + DogInspectScreen.TRUST_ROW_HEIGHT,
        DogInspectScreen.cardHeight(WildTrustView.SHUNNED));
    assertEquals(
        DogInspectScreen.CARD_HEIGHT + DogInspectScreen.TRUST_ROW_HEIGHT,
        DogInspectScreen.cardHeight(WildTrustView.building(10)));
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
        contentBottom + DogInspectScreen.TRUST_ROW_HEIGHT
            <= DogInspectScreen.cardHeight(WildTrustView.building(1))
                - DogInspectScreen.CARD_PADDING,
        "the trust row overflows the card");
  }
}
