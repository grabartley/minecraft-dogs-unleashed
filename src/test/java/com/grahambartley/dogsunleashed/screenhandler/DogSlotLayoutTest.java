package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogSlotLayoutTest {

  @ParameterizedTest(name = "slot {0} sits at ({1}, {2})")
  @CsvSource({"0, 8, 18", "1, 8, 36", "2, 8, 54", "3, 80, 54"})
  @DisplayName(
      "armour, pendant and cosmetic fill the left column and the costume sits beside the cosmetic")
  void slotPositions(final int index, final int x, final int y) {
    assertEquals(x, DogSlotLayout.x(index));
    assertEquals(y, DogSlotLayout.y(index));
  }
}
