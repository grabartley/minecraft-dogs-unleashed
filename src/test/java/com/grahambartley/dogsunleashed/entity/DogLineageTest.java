package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogLineage.MateState;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogLineageTest {

  @ParameterizedTest(name = "{0} paired with itself stays pure")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"CROSS_BREED"},
      mode = EnumSource.Mode.EXCLUDE)
  @DisplayName("two parents of the same pure breed produce that breed")
  void samePureBreedProducesThatBreed(final UnleashedDogBreed breed) {
    assertEquals(breed, DogLineage.childBreed(breed, breed));
  }

  static Stream<Arguments> mixedPairs() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, UnleashedDogBreed.BEAGLE),
        Arguments.of(UnleashedDogBreed.BEAGLE, UnleashedDogBreed.HUSKY),
        Arguments.of(UnleashedDogBreed.DACHSHUND, UnleashedDogBreed.SHIBA_INU),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, UnleashedDogBreed.DACHSHUND));
  }

  @ParameterizedTest(name = "{0} with {1}")
  @MethodSource("mixedPairs")
  @DisplayName("two different pure breeds produce a cross breed")
  void differentPureBreedsProduceACrossBreed(
      final UnleashedDogBreed first, final UnleashedDogBreed second) {
    assertEquals(UnleashedDogBreed.CROSS_BREED, DogLineage.childBreed(first, second));
  }

  @ParameterizedTest(name = "{0} with a cross breed")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a cross breed parent always produces a cross breed, on either side")
  void aCrossBreedParentAlwaysProducesACrossBreed(final UnleashedDogBreed other) {
    assertEquals(
        UnleashedDogBreed.CROSS_BREED, DogLineage.childBreed(UnleashedDogBreed.CROSS_BREED, other));
    assertEquals(
        UnleashedDogBreed.CROSS_BREED, DogLineage.childBreed(other, UnleashedDogBreed.CROSS_BREED));
  }

  private static MateState ready() {
    return new MateState(true, false, true);
  }

  static Stream<Arguments> blockedPairs() {
    return Stream.of(
        Arguments.of("the initiator is untamed", new MateState(false, false, true), ready()),
        Arguments.of("the partner is untamed", ready(), new MateState(false, false, true)),
        Arguments.of("the partner is sitting", ready(), new MateState(true, true, true)),
        Arguments.of("the initiator is not in love", new MateState(true, false, false), ready()),
        Arguments.of("the partner is not in love", ready(), new MateState(true, false, false)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("blockedPairs")
  @DisplayName("a pair does not breed when either dog is not ready")
  void aPairDoesNotBreedWhenEitherDogIsNotReady(
      final String label, final MateState self, final MateState partner) {
    assertFalse(DogLineage.matesCanBreed(self, partner));
  }

  @Test
  @DisplayName("two tamed standing dogs in love breed")
  void twoTamedStandingDogsInLoveBreed() {
    assertTrue(DogLineage.matesCanBreed(ready(), ready()));
  }

  @Test
  @DisplayName("the initiator's own sitting pose does not block the pair")
  void theInitiatorsSittingPoseDoesNotBlockThePair() {
    assertTrue(DogLineage.matesCanBreed(new MateState(true, true, true), ready()));
  }
}
