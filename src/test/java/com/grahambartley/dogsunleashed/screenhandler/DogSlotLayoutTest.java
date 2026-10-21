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
    "0, 106, 18",
    "1, 124, 18",
    "2, 142, 18",
    "3, 106, 36",
    "4, 124, 36",
    "5, 142, 36",
    "6, 106, 54",
    "7, 124, 54",
    "8, 142, 54"
  })
  @DisplayName("the nine bag slots form a three by three grid right of the gear")
  void bagPositions(final int index, final int x, final int y) {
    assertEquals(x, DogSlotLayout.bagX(index));
    assertEquals(y, DogSlotLayout.bagY(index));
  }
}
