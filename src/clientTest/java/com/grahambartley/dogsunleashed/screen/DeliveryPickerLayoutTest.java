package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveryPickerLayoutTest {

  @ParameterizedTest(name = "{0} recipients -> {1} pages")
  @CsvSource({"0, 1", "1, 1", "6, 1", "7, 2", "12, 2", "13, 3", "64, 11"})
  @DisplayName("six recipients fit on a page and an empty list still shows one page")
  void pageCount(final int recipients, final int expected) {
    assertEquals(expected, DeliveryPickerLayout.pageCount(recipients));
  }

  @ParameterizedTest(name = "page {0} of {1} recipients shows {2}")
  @CsvSource({"0, 0, 0", "0, 4, 4", "0, 6, 6", "0, 9, 6", "1, 9, 3", "2, 9, 0"})
  @DisplayName("the last page shows only what is left")
  void shownOnPage(final int page, final int recipients, final int expected) {
    assertEquals(expected, DeliveryPickerLayout.shownOnPage(page, recipients));
  }

  @ParameterizedTest(name = "page {0} with {1} recipients clamps to {2}")
  @CsvSource({"-1, 9, 0", "0, 9, 0", "1, 9, 1", "5, 9, 1", "3, 0, 0"})
  @DisplayName("a page out of range clamps back in, for example after players leave")
  void clampPage(final int page, final int recipients, final int expected) {
    assertEquals(expected, DeliveryPickerLayout.clampPage(page, recipients));
  }

  @Test
  @DisplayName("rows step down evenly and the footer sits below a full page")
  void rowsAndFooter() {
    final int height = 240;
    assertEquals(
        DeliveryPickerLayout.ROW_PITCH,
        DeliveryPickerLayout.rowY(height, 1) - DeliveryPickerLayout.rowY(height, 0));
    assertEquals(
        DeliveryPickerLayout.rowY(height, DeliveryPickerLayout.PAGE_SIZE),
        DeliveryPickerLayout.footerY(height));
    assertEquals(56, DeliveryPickerLayout.listTop(height));
  }
}
