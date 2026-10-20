package com.grahambartley.dogsunleashed.entity.scent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScentRangeTest {

  @ParameterizedTest
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a beagle tracks 64 blocks and every other breed 32")
  void pureBreedRange(final UnleashedDogBreed breed) {
    assertEquals(breed == UnleashedDogBreed.BEAGLE ? 64 : 32, ScentRange.of(breed, List.of()));
  }

  static Stream<Arguments> crosses() {
    return Stream.of(
        Arguments.of("half beagle", 0.5f, 0.5f, 48),
        Arguments.of("quarter beagle", 0.25f, 0.75f, 40),
        Arguments.of("all beagle", 1.0f, 0.0f, 64),
        Arguments.of("no beagle", 0.0f, 1.0f, 32));
  }

  @ParameterizedTest(name = "{0} -> {3}")
  @MethodSource("crosses")
  @DisplayName("a cross takes a range weighted by its genome")
  void crossRangeIsWeighted(
      final String label, final float beagle, final float husky, final int expected) {
    assertEquals(
        expected,
        ScentRange.of(
            UnleashedDogBreed.CROSS_BREED,
            List.of(
                new BreedShare(UnleashedDogBreed.BEAGLE, beagle),
                new BreedShare(UnleashedDogBreed.HUSKY, husky))));
  }
}
