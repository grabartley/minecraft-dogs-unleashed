package com.grahambartley.dogsunleashed.screen;

record ConfigScreenLayout(int listTop, int listBottom, int buttonsY) {
  static final int TITLE_Y = 16;
  static final int BANNER_Y = 30;
  static final int BANNER_LINE_HEIGHT = 12;
  static final int TEXT_HEIGHT = 9;
  static final int LIST_GAP = 7;
  static final int BUTTON_HEIGHT = 20;
  static final int FOOTER_HEIGHT = 32;

  static ConfigScreenLayout of(final int screenHeight, final boolean showsBanner) {
    final int listTop = headerBottom(showsBanner) + LIST_GAP;
    final int footerTop = screenHeight - FOOTER_HEIGHT;
    final int listBottom = Math.max(listTop, footerTop);
    final int buttonsY = footerTop + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    return new ConfigScreenLayout(listTop, listBottom, buttonsY);
  }

  static int headerBottom(final boolean showsBanner) {
    return showsBanner ? BANNER_Y + BANNER_LINE_HEIGHT + TEXT_HEIGHT : TITLE_Y + TEXT_HEIGHT;
  }

  int listHeight() {
    return this.listBottom - this.listTop;
  }
}
