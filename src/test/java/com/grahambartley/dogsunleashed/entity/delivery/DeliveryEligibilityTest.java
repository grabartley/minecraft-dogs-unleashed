package com.grahambartley.dogsunleashed.entity.delivery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DeliveryEligibilityTest {

  @ParameterizedTest(name = "bags {0}, others {1}, ridden {2}, asleep {3}, sitting {4}, lead {5}")
  @CsvSource(
      nullValues = "none",
      value = {
        "true, true, false, false, false, false, none",
        "false, true, false, false, false, false, no_saddlebags",
        "false, false, true, true, true, true, no_saddlebags",
        "true, false, true, false, false, false, no_one_online",
        "true, true, true, true, false, false, ridden",
        "true, true, false, true, true, false, sleeping",
        "true, true, false, false, true, true, sitting",
        "true, true, false, false, false, true, leashed"
      })
  @DisplayName("the first unmet need is the reason the wheel greys out Deliver")
  void refusal(
      final boolean bags,
      final boolean others,
      final boolean ridden,
      final boolean sleeping,
      final boolean sitting,
      final boolean leashed,
      final String expected) {
    assertEquals(
        Optional.ofNullable(expected)
            .map(reason -> "screen.dogs-unleashed.command_wheel.deliver." + reason),
        DeliveryEligibility.refusalKey(bags, others, ridden, sleeping, sitting, leashed));
  }
}
