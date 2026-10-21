package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SledTeamCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:sled_team")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:sled_team", SledTeamCriterion.ID.toString());
  }

  @ParameterizedTest(name = "team of {0} against a minimum of {1} -> {2}")
  @CsvSource({"6, 6, true", "5, 6, false", "1, 1, true", "3, , true"})
  @DisplayName("a team matches once it reaches the minimum size")
  void matches(final int team, final Integer minimum, final boolean expected) {
    assertEquals(
        expected,
        new SledTeamCriterion.Conditions(Optional.empty(), Optional.ofNullable(minimum))
            .matches(team));
  }
}
