package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogSlotLayoutTest {

  @ParameterizedTest(name = "slot {0} sits at ({1}, {2})")
  @CsvSource({"0, 8, 18", "1, 8, 36", "2, 8, 54", "3, 80, 54", "4, 80, 18"})
  @DisplayName(
      "armour, pendant and cosmetic fill the left column, the costume and saddlebags the side")
  void slotPositions(final int index, final int x, final int y) {
    assertEquals(x, DogSlotLayout.x(index));
    assertEquals(y, DogSlotLayout.y(index));
  }

  @ParameterizedTest(name = "bag slot {0} sits at ({1}, {2})")
  @CsvSource({
    "0, 98, 18",
    "1, 116, 18",
    "2, 134, 18",
    "3, 98, 36",
    "4, 116, 36",
    "5, 134, 36",
    "6, 98, 54",
    "7, 116, 54",
    "8, 134, 54"
  })
  @DisplayName(
      "the nine bag slots form a three by three grid on the chest panel, beside the side column")
  void bagPositions(final int index, final int x, final int y) {
    assertEquals(x, DogSlotLayout.bagX(index));
    assertEquals(y, DogSlotLayout.bagY(index));
  }
}
