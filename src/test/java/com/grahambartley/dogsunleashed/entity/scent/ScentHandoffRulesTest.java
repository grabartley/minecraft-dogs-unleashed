package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.scent.ScentHandoffRules.Handoff;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScentHandoffRulesTest {

  @ParameterizedTest(name = "map={0} scent={1} taming={2} tracking={3} mark={4} same={5} -> {6}")
  @CsvSource({
    "false, false, false, false, false, false, IGNORE",
    "false, true, true, false, false, false, IGNORE",
    "true, false, true, false, true, false, IGNORE",
    "false, true, false, false, false, false, FOLLOW_SCENT",
    "false, true, false, true, false, false, CALL_OFF",
    "true, false, false, false, false, false, IGNORE",
    "true, false, false, false, true, false, FOLLOW_MAP",
    "true, false, false, true, true, false, FOLLOW_MAP",
    "true, false, false, true, true, true, CALL_OFF",
    "true, false, false, false, true, true, FOLLOW_MAP"
  })
  @DisplayName("handing over an item starts, switches or calls off the track")
  void decidesWhatAHandedItemDoes(
      final boolean map,
      final boolean scentItem,
      final boolean tamingItem,
      final boolean trackingThisItem,
      final boolean markFound,
      final boolean sameMark,
      final Handoff expected) {
    assertEquals(
        expected,
        ScentHandoffRules.decide(
            map, scentItem, tamingItem, trackingThisItem, markFound, sameMark));
  }
}
