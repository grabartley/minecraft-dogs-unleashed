package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogExpeditionReturnedCriterionTest {

  @ParameterizedTest(name = "wants rare={0}, find rare={1} -> {2}")
  @CsvSource({
    ", false, true",
    ", true, true",
    "true, true, true",
    "true, false, false",
    "false, false, true",
    "false, true, false"
  })
  @DisplayName("the rare find condition matches only the kind of find it names")
  void rareFindConditionMatches(final Boolean wanted, final boolean rare, final boolean expected) {
    assertEquals(
        expected,
        new DogExpeditionReturnedCriterion.Conditions(Optional.empty(), Optional.ofNullable(wanted))
            .matches(rare));
  }
}
