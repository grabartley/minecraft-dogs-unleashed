package com.grahambartley.dogsunleashed.advancement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogTamedCriterionTest {

  @Test
  @DisplayName("the criterion id is dogs-unleashed:dog_tamed")
  void exposesItsIdentifier() {
    assertEquals("dogs-unleashed:dog_tamed", DogTamedCriterion.ID.toString());
  }

  static Stream<Arguments> matches() {
    return Stream.of(
        Arguments.of(Optional.empty(), Optional.empty(), "husky", "white", true),
        Arguments.of(Optional.of("direwolf"), Optional.empty(), "direwolf", "black", true),
        Arguments.of(Optional.of("direwolf"), Optional.empty(), "husky", "black", false),
        Arguments.of(Optional.of("direwolf"), Optional.of("white"), "direwolf", "white", true),
        Arguments.of(Optional.of("direwolf"), Optional.of("white"), "direwolf", "silver", false),
        Arguments.of(Optional.of("direwolf"), Optional.of("white"), "dachshund", "white", false),
        Arguments.of(Optional.empty(), Optional.of("black"), "shibainu", "black", true));
  }

  @ParameterizedTest(name = "breed={0} coat={1} vs tamed {2} {3} -> {4}")
  @MethodSource("matches")
  @DisplayName("conditions match only the breed and coat they name")
  void conditionsMatchNamedBreedAndCoat(
      final Optional<String> breed,
      final Optional<String> coat,
      final String tamedBreed,
      final String tamedCoat,
      final boolean expected) {
    assertEquals(
        expected,
        new DogTamedCriterion.Conditions(Optional.empty(), breed, coat)
            .matches(tamedBreed, tamedCoat));
  }
}
