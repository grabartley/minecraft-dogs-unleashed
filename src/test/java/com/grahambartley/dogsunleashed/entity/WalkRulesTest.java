package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WalkRulesTest {

  @ParameterizedTest(name = "limb speed {0} -> walks {1}")
  @CsvSource({"0.0, false", "0.1, false", "0.15, false", "0.16, true", "0.4, true", "1.0, true"})
  @DisplayName("dogs walk once their legs swing faster than a stand-still shuffle")
  void walks(final float limbSpeed, final boolean expected) {
    assertEquals(expected, WalkRules.walks(limbSpeed));
  }
}
