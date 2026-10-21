package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DogTrickLearnedCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:dog_trick_learned")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_trick_learned", DogTrickLearnedCriterion.ID.toString());
  }

  @ParameterizedTest(name = "min={0}, learned={1} -> {2}")
  @CsvSource({", 1, true", ", 3, true", "3, 1, false", "3, 2, false", "3, 3, true", "1, 1, true"})
  @DisplayName("the minimum learned condition matches once the dog knows enough tricks")
  void minLearnedConditionMatches(
      final Integer minLearned, final int learned, final boolean expected) {
    assertEquals(
        expected,
        new DogTrickLearnedCriterion.Conditions(Optional.empty(), Optional.ofNullable(minLearned))
            .matches(learned));
  }
}
