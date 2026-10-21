package com.grahambartley.dogsunleashed.pet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class PetAwayStatusTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(PetAwayStatus.class)
  @DisplayName("every status round-trips through its wire id")
  void idRoundTrip(final PetAwayStatus status) {
    assertSame(status, PetAwayStatus.byId(status.ordinal()));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(ints = {-1, 5, 99})
  @DisplayName("an unknown wire id reads as home")
  void unknownIdIsHome(final int id) {
    assertSame(PetAwayStatus.HOME, PetAwayStatus.byId(id));
  }

  @ParameterizedTest(name = "{0} -> away {1}, label {2}")
  @CsvSource(
      nullValues = "none",
      value = {
        "HOME, false, none",
        "EXPLORING, true, screen.dogs-unleashed.pet_manager.exploring",
        "DELIVERING, true, screen.dogs-unleashed.pet_manager.delivering",
        "WAITING, true, screen.dogs-unleashed.pet_manager.waiting",
        "RETURNING, true, screen.dogs-unleashed.pet_manager.returning"
      })
  @DisplayName("only a dog at home has no status label")
  void labels(final PetAwayStatus status, final boolean away, final String key) {
    assertEquals(away, status.isAway());
    assertEquals(key, status.translationKey());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource(
      nullValues = "none",
      value = {
        "HOME, none",
        "EXPLORING, message.dogs-unleashed.expedition.away",
        "DELIVERING, message.dogs-unleashed.delivery.away",
        "WAITING, message.dogs-unleashed.delivery.away",
        "RETURNING, message.dogs-unleashed.delivery.away"
      })
  @DisplayName("a summon for a dog that is away explains where it went")
  void busyKey(final PetAwayStatus status, final String expected) {
    assertEquals(Optional.ofNullable(expected), status.busyKey());
  }
}
