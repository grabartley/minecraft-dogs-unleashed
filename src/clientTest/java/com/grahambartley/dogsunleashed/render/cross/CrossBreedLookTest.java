package com.grahambartley.dogsunleashed.render.cross;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.render.cross.CrossBreedLook.Influence;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrossBreedLookTest {

  private static final int SAMPLES = 4000;

  private static List<BreedShare> mix(final float beagle, final float husky) {
    return List.of(
        new BreedShare(UnleashedDogBreed.HUSKY, husky),
        new BreedShare(UnleashedDogBreed.BEAGLE, beagle));
  }

  @Test
  @DisplayName("a single breed is pure and stands on its own rig")
  void singleBreedIsPure() {
    final CrossBreedLook look =
        CrossBreedLook.of(List.of(new BreedShare(UnleashedDogBreed.SHIBA_INU, 1.0f)), 0);

    assertTrue(look.isPure());
    assertEquals(UnleashedDogBreed.SHIBA_INU, look.rig());
    assertEquals(UnleashedDogBreed.SHIBA_INU, look.earSource());
  }

  @Test
  @DisplayName("a cross stands on its largest share and is pulled toward the rest by their shares")
  void crossIsPulledByTheOtherShares() {
    final CrossBreedLook look = CrossBreedLook.of(mix(0.75f, 0.25f), 0);

    assertEquals(UnleashedDogBreed.BEAGLE, look.rig());
    assertEquals(List.of(new Influence(UnleashedDogBreed.HUSKY, 0.25f)), look.influences());
  }

  @ParameterizedTest(name = "{0} husky")
  @CsvSource({"0.5", "0.25", "0.125"})
  @DisplayName("ears come whole from one breed, picked about as often as its share")
  void earsFollowTheShares(final float husky) {
    final long huskyEars =
        IntStream.range(0, SAMPLES)
            .mapToObj(coat -> CrossBreedLook.of(mix(1.0f - husky, husky), coat).earSource())
            .filter(UnleashedDogBreed.HUSKY::equals)
            .count();

    assertEquals(husky, huskyEars / (float) SAMPLES, 0.03f);
  }

  @Test
  @DisplayName("the same dog always gets the same look, and a different coat or share another")
  void lookIsStablePerDog() {
    assertEquals(
        CrossBreedLook.of(mix(0.75f, 0.25f), 3).seed(),
        CrossBreedLook.of(mix(0.75f, 0.25f), 3).seed());
    assertNotEquals(
        CrossBreedLook.of(mix(0.75f, 0.25f), 3).seed(),
        CrossBreedLook.of(mix(0.75f, 0.25f), 4).seed());
    assertNotEquals(
        CrossBreedLook.of(mix(0.75f, 0.25f), 3).seed(),
        CrossBreedLook.of(mix(0.625f, 0.375f), 3).seed());
  }

  @ParameterizedTest(name = "{0} coats")
  @CsvSource({"1", "3", "6", "12"})
  @DisplayName("the other breed's coat is one of its real coats, and every coat turns up")
  void otherCoatIsOneOfItsCoats(final int coats) {
    final long seen =
        IntStream.range(0, SAMPLES)
            .map(
                coat ->
                    CrossBreedLook.of(mix(0.5f, 0.5f), coat)
                        .coatIndexFor(UnleashedDogBreed.HUSKY, coats))
            .peek(index -> assertTrue(index >= 0 && index < coats))
            .distinct()
            .count();

    assertEquals(coats, seen);
  }
}
