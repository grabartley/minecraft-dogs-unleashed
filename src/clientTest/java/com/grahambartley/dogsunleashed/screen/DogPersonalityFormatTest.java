package com.grahambartley.dogsunleashed.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.personality.DogTrait;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DogPersonalityFormatTest {

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(DogTraits.NONE, ""),
        Arguments.of(DogTraits.of(DogTrait.BRAVE), "brave"),
        Arguments.of(DogTraits.of(DogTrait.GREEDY, DogTrait.CLINGY), "greedy, clingy"));
  }

  @ParameterizedTest(name = "{0} -> \"{1}\"")
  @MethodSource("cases")
  @DisplayName("traits are listed in order, comma separated")
  void format(final DogTraits traits, final String expected) {
    assertEquals(expected, DogPersonalityFormat.format(traits, DogTrait::serializedId));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("cases")
  @DisplayName("the translated form names each trait by its key")
  void translatedFormUsesTraitKeys(final DogTraits traits, final String ignored) {
    assertEquals(
        DogPersonalityFormat.format(traits, DogTrait::translationKey),
        DogPersonalityFormat.format(traits));
  }
}
