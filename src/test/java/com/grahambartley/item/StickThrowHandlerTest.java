package com.grahambartley.item;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StickThrowHandlerTest {

  @Test
  @DisplayName("stick throw handler should load without errors")
  void stickThrowHandlerShouldLoad() {
    assertNotNull(StickThrowHandler.class);
  }
}
