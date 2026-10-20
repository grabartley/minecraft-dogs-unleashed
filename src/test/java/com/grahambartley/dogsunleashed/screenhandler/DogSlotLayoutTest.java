package com.grahambartley.dogsunleashed.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogSlotLayoutTest {

  @ParameterizedTest(name = "slot {0} sits at ({1}, {2})")
  @CsvSource({"0, 8, 18", "1, 8, 36", "2, 8, 54", "3, 80, 54", "4, 80, 36", "5, 80, 18"})
  @DisplayName("the first three slots fill the left column and later ones climb the side column")
  void slotPositions(final int index, final int x, final int y) {
    assertEquals(x, DogSlotLayout.x(index));
    assertEquals(y, DogSlotLayout.y(index));
  }
}
