package com.grahambartley.item;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrisbeeItemTest {

  @Test
  @DisplayName("frisbee item should extend Item")
  void frisbeeItemShouldExtendItem() {
    assertTrue(FrisbeeItem.class.getSuperclass() == Item.class);
  }
}
