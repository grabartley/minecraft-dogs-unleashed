package com.grahambartley.dogsunleashed.screenhandler;

public final class DogSlotLayout {

  public static final int SLOT_SPACING = 18;
  public static final int BAG_COLUMNS = 3;
  static final int LEFT_COLUMN_X = 8;
  static final int SIDE_COLUMN_X = 80;
  static final int BAG_GRID_X = 98;
  static final int FIRST_ROW_Y = 18;
  static final int LEFT_COLUMN_ROWS = 3;
  private static final int[] SIDE_COLUMN_ROWS = {2, 0};

  private DogSlotLayout() {}

  public static int x(final int index) {
    return index < LEFT_COLUMN_ROWS ? LEFT_COLUMN_X : SIDE_COLUMN_X;
  }

  public static int y(final int index) {
    final int row = index < LEFT_COLUMN_ROWS ? index : SIDE_COLUMN_ROWS[index - LEFT_COLUMN_ROWS];
    return FIRST_ROW_Y + row * SLOT_SPACING;
  }

  public static int bagX(final int index) {
    return BAG_GRID_X + (index % BAG_COLUMNS) * SLOT_SPACING;
  }

  public static int bagY(final int index) {
    return FIRST_ROW_Y + (index / BAG_COLUMNS) * SLOT_SPACING;
  }
}
