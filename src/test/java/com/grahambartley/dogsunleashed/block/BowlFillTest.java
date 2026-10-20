package com.grahambartley.dogsunleashed.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BowlFillTest {

  @ParameterizedTest(name = "{0} is named {1} in the blockstate file")
  @CsvSource({"EMPTY, empty", "SOME, some", "FULL, full"})
  @DisplayName("each fill level uses the name the blockstate variants are keyed by")
  void eachFillHasItsBlockstateName(final BowlFill fill, final String name) {
    assertEquals(name, fill.asString());
  }
}
