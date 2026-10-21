package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveryWaitRulesTest {

  @ParameterizedTest(name = "present {0}, same world {1}, {2} blocks, wait over {3} -> home {4}")
  @CsvSource({
    "true, true, 3, false, false",
    "true, true, 48, false, false",
    "true, true, 48.5, false, true",
    "true, true, 3, true, true",
    "true, false, 3, false, true",
    "false, true, 3, false, true"
  })
  @DisplayName("a dog waits beside its recipient until they leave, wander off or time runs out")
  void headsHome(
      final boolean present,
      final boolean sameWorld,
      final double distance,
      final boolean waitOver,
      final boolean expected) {
    assertEquals(expected, DeliveryWaitRules.headsHome(present, sameWorld, distance, waitOver));
  }
}
