package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.dogsunleashed.pet.PetAwayStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeliveryLegTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(DeliveryLeg.class)
  @DisplayName("every leg round-trips through its saved id")
  void idRoundTrip(final DeliveryLeg leg) {
    assertSame(leg, DeliveryLeg.fromId(leg.id()));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(strings = {"", "WAITING", "home"})
  @DisplayName("an unknown id resolves to null")
  void unknownId(final String id) {
    assertNull(DeliveryLeg.fromId(id));
  }

  @ParameterizedTest(name = "{0} off world {1}")
  @CsvSource({"TO_RECIPIENT, true", "WAITING, false", "TO_OWNER, true"})
  @DisplayName("the dog is out of the world while travelling and in it while waiting")
  void offWorld(final DeliveryLeg leg, final boolean expected) {
    assertEquals(expected, leg.offWorld());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"TO_RECIPIENT, DELIVERING", "WAITING, WAITING", "TO_OWNER, RETURNING"})
  @DisplayName("each leg shows its own status in the Pet Manager")
  void awayStatus(final DeliveryLeg leg, final PetAwayStatus expected) {
    assertSame(expected, leg.awayStatus());
  }
}
