package com.grahambartley.dogsunleashed.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogAppearanceRollerTest {

  static Stream<Arguments> secondIndexShifts() {
    return Stream.of(
        Arguments.of("a raw draw below the first is taken as is", 3, 0, 0),
        Arguments.of("a raw draw just below the first is taken as is", 3, 2, 2),
        Arguments.of("a raw draw equal to the first is pushed past it", 3, 3, 4),
        Arguments.of("a raw draw above the first is pushed past it", 3, 4, 5),
        Arguments.of("a first draw of zero pushes every raw draw up", 0, 0, 1));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("secondIndexShifts")
  @DisplayName("the second founder draw shifts past the first")
  void secondIndexShiftsPastTheFirst(
      final String label, final int firstIndex, final int rawSecondIndex, final int expected) {
    assertEquals(expected, DogAppearanceRoller.distinctSecondIndex(firstIndex, rawSecondIndex));
  }

  @Test
  @DisplayName("every raw draw maps to a distinct founder exactly once, whatever the first was")
  void everyRawDrawMapsToADistinctFounderExactlyOnce() {
    final int founderCount = DogAppearanceRoller.naturalFounders().size();
    for (int firstIndex = 0; firstIndex < founderCount; firstIndex++) {
      final Set<Integer> seconds = new HashSet<>();
      for (int rawSecondIndex = 0; rawSecondIndex < founderCount - 1; rawSecondIndex++) {
        final int secondIndex = DogAppearanceRoller.distinctSecondIndex(firstIndex, rawSecondIndex);
        assertTrue(
            secondIndex >= 0 && secondIndex < founderCount,
            "first=" + firstIndex + " raw=" + rawSecondIndex + " landed outside the founder list");
        assertNotEquals(
            firstIndex, secondIndex, "a dog would be crossed with its own founder breed");
        assertTrue(seconds.add(secondIndex), "two raw draws collided on founder " + secondIndex);
      }
      assertEquals(
          founderCount - 1,
          seconds.size(),
          "every founder except the first should be reachable from first=" + firstIndex);
    }
  }

  @Test
  @DisplayName("the founder pool holds only naturally spawning breeds, and enough to cross two")
  void founderPoolIsNaturallySpawningAndBigEnoughToCross() {
    final List<UnleashedDogBreed> founders = DogAppearanceRoller.naturalFounders();
    assertTrue(founders.size() >= 2, "a founder mix needs two distinct breeds to draw from");
    assertTrue(
        founders.stream().allMatch(UnleashedDogBreed::isNaturallySpawning),
        "a non-spawning breed would seed a cross that cannot occur in the world");
    assertTrue(
        founders.stream().noneMatch(breed -> breed == UnleashedDogBreed.CROSS_BREED),
        "a cross breed cannot found itself");
  }

  @ParameterizedTest(name = "current ordinal {0}")
  @ValueSource(ints = {0, 1, 7})
  @DisplayName("a breed with nothing to roll keeps the ordinal it already has")
  void unrollableVariantKeepsItsCurrentOrdinal(final int currentOrdinal) {
    assertEquals(
        currentOrdinal,
        DogAppearanceRoller.resolvedVariantOrdinal(false, currentOrdinal, () -> 99));
  }

  @Test
  @DisplayName("a rollable variant takes the rolled ordinal over its current one")
  void rollableVariantTakesTheRolledOrdinal() {
    assertEquals(99, DogAppearanceRoller.resolvedVariantOrdinal(true, 3, () -> 99));
  }

  @Test
  @DisplayName("an unrollable variant never touches the entity rng")
  void unrollableVariantNeverDrawsFromTheRng() {
    final AtomicInteger draws = new AtomicInteger();
    DogAppearanceRoller.resolvedVariantOrdinal(false, 3, draws::incrementAndGet);
    assertEquals(0, draws.get(), "an extra draw would shift every later roll in the same tick");
  }

  @Test
  @DisplayName("a rollable variant draws from the entity rng exactly once")
  void rollableVariantDrawsExactlyOnce() {
    final AtomicInteger draws = new AtomicInteger();
    DogAppearanceRoller.resolvedVariantOrdinal(true, 3, draws::incrementAndGet);
    assertEquals(1, draws.get());
  }
}
