package com.grahambartley.dogsunleashed.screen;

record ConfigScreenLayout(int listTop, int listBottom, int buttonsY) {
  static final int BUTTON_HEIGHT = 20;
  static final int EDITABLE_LIST_TOP = 32;
  static final int READ_ONLY_LIST_TOP = 58;
  static final int FOOTER_HEIGHT = 32;

  static ConfigScreenLayout of(final int screenHeight, final boolean showsBanner) {
    final int listTop = showsBanner ? READ_ONLY_LIST_TOP : EDITABLE_LIST_TOP;
    final int footerTop = screenHeight - FOOTER_HEIGHT;
    final int listBottom = Math.max(listTop, footerTop);
    final int buttonsY = footerTop + (FOOTER_HEIGHT - BUTTON_HEIGHT) / 2;
    return new ConfigScreenLayout(listTop, listBottom, buttonsY);
  }

  int listHeight() {
    return this.listBottom - this.listTop;
  }
}
