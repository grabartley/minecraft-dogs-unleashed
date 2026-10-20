package com.grahambartley.dogsunleashed.entity.expedition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpeditionEligibilityTest {

  @ParameterizedTest(name = "undead={0} ridden={1} sleeping={2} sitting={3} leashed={4} -> {5}")
  @CsvSource({
    "false, false, false, false, false, ",
    "true, false, false, false, false, screen.dogs-unleashed.command_wheel.explore.undead",
    "false, true, false, false, false, screen.dogs-unleashed.command_wheel.explore.ridden",
    "false, false, true, false, false, screen.dogs-unleashed.command_wheel.explore.sleeping",
    "false, false, false, true, false, screen.dogs-unleashed.command_wheel.explore.sitting",
    "false, false, false, false, true, screen.dogs-unleashed.command_wheel.explore.leashed",
    "true, true, true, true, true, screen.dogs-unleashed.command_wheel.explore.undead",
    "false, false, true, true, false, screen.dogs-unleashed.command_wheel.explore.sleeping"
  })
  @DisplayName("a dog that is undead, ridden, asleep, sitting or leashed can't set off")
  void refusalKey(
      final boolean undead,
      final boolean ridden,
      final boolean sleeping,
      final boolean sitting,
      final boolean leashed,
      final String expected) {
    assertEquals(
        Optional.ofNullable(expected),
        ExpeditionEligibility.refusalKey(undead, ridden, sleeping, sitting, leashed));
  }
}
