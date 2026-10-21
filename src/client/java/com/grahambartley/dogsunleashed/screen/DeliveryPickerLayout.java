package com.grahambartley.dogsunleashed.screen;

public final class DeliveryPickerLayout {

  public static final int PAGE_SIZE = 6;
  public static final int BUTTON_WIDTH = 160;
  public static final int BUTTON_HEIGHT = 20;
  public static final int ROW_PITCH = 24;
  static final int TITLE_GAP = 20;

  private DeliveryPickerLayout() {}

  public static int pageCount(final int recipients) {
    return Math.max(1, (recipients + PAGE_SIZE - 1) / PAGE_SIZE);
  }

  public static int clampPage(final int page, final int recipients) {
    return Math.max(0, Math.min(page, pageCount(recipients) - 1));
  }

  public static int firstOnPage(final int page) {
    return page * PAGE_SIZE;
  }

  public static int shownOnPage(final int page, final int recipients) {
    return Math.max(0, Math.min(PAGE_SIZE, recipients - firstOnPage(page)));
  }

  public static int listTop(final int screenHeight) {
    return (screenHeight - (PAGE_SIZE + 1) * ROW_PITCH) / 2 + TITLE_GAP;
  }

  public static int rowY(final int screenHeight, final int row) {
    return listTop(screenHeight) + row * ROW_PITCH;
  }

  public static int footerY(final int screenHeight) {
    return rowY(screenHeight, PAGE_SIZE);
  }
}
