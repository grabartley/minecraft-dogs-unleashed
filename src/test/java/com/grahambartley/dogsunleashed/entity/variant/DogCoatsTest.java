package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogCoatsTest {

  static Stream<Arguments> coatLookups() {
    return Stream.of(
        Arguments.of("husky white", UnleashedDogBreed.HUSKY, HuskyCoat.WHITE),
        Arguments.of("dachshund albino", UnleashedDogBreed.DACHSHUND, DachshundCoat.ALBINO),
        Arguments.of("beagle lilac 2", UnleashedDogBreed.BEAGLE, BeagleCoat.LILAC_2),
        Arguments.of("shiba sesame", UnleashedDogBreed.SHIBA_INU, ShibaInuCoat.SESAME));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("coatLookups")
  @DisplayName("coats resolve from a breed and ordinal")
  void coatsResolveFromBreedAndOrdinal(
      final String label, final UnleashedDogBreed breed, final UnleashedDogCoat coat) {
    assertEquals(coat, DogCoats.coatOf(breed, coat.getOrdinal()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("an unset ordinal never resolves to a coat")
  void unsetOrdinalResolvesToNull(final UnleashedDogBreed breed) {
    assertNull(DogCoats.coatOf(breed, -1));
  }

  @Test
  @DisplayName("the golden retriever has no coats to resolve")
  void goldenRetrieverHasNoCoats() {
    assertNull(DogCoats.coatOf(UnleashedDogBreed.GOLDEN_RETRIEVER, 0));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(UnleashedDogBreed.class)
  @DisplayName("a roll resolver exists exactly for breeds with coat variants")
  void rollResolverExistsExactlyForBreedsWithCoatVariants(final UnleashedDogBreed breed) {
    assertEquals(DogCoats.hasCoatVariants(breed), DogCoats.rollResolverFor(breed) != null);
    assertEquals(breed != UnleashedDogBreed.GOLDEN_RETRIEVER, DogCoats.hasCoatVariants(breed));
  }

  static Stream<Arguments> rollTables() {
    return Stream.of(
        Arguments.of(
            UnleashedDogBreed.HUSKY,
            (BiFunction<SpawnReason, Integer, UnleashedDogCoat>)
                HuskyCoatRolls::resolveCoatFromRoll),
        Arguments.of(
            UnleashedDogBreed.DACHSHUND,
            (BiFunction<SpawnReason, Integer, UnleashedDogCoat>)
                DachshundCoatRolls::resolveCoatFromRoll),
        Arguments.of(
            UnleashedDogBreed.BEAGLE,
            (BiFunction<SpawnReason, Integer, UnleashedDogCoat>)
                BeagleCoatRolls::resolveCoatFromRoll),
        Arguments.of(
            UnleashedDogBreed.SHIBA_INU,
            (BiFunction<SpawnReason, Integer, UnleashedDogCoat>)
                ShibaInuCoatRolls::resolveCoatFromRoll));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("rollTables")
  @DisplayName("each breed's roll resolver matches its roll table for every roll")
  void rollResolverMatchesBreedRollTable(
      final UnleashedDogBreed breed,
      final BiFunction<SpawnReason, Integer, UnleashedDogCoat> rollTable) {
    final BiFunction<SpawnReason, Integer, UnleashedDogCoat> resolver =
        DogCoats.rollResolverFor(breed);
    assertNotNull(resolver);
    for (final SpawnReason spawnReason : List.of(SpawnReason.NATURAL, SpawnReason.BREEDING)) {
      for (int roll = 0; roll < DogCoats.ROLL_BOUND; roll++) {
        assertEquals(
            rollTable.apply(spawnReason, roll),
            resolver.apply(spawnReason, roll),
            breed + " " + spawnReason + " roll " + roll);
      }
    }
  }

  @Test
  @DisplayName("every coat translation key is unique and breed scoped")
  void coatTranslationKeysAreUniqueAndBreedScoped() {
    final Set<String> keys = new HashSet<>();
    for (final UnleashedDogBreed breed : UnleashedDogBreed.values()) {
      int ordinal = 0;
      UnleashedDogCoat coat;
      while ((coat = DogCoats.coatOf(breed, ordinal)) != null && coat.getOrdinal() == ordinal) {
        final String key = coat.translationKey();
        assertNotNull(key);
        assertTrue(
            key.startsWith("coat.dogs-unleashed." + breed.serializedId() + "."),
            key + " is not scoped to " + breed.serializedId());
        assertTrue(keys.add(key), key + " is duplicated");
        ordinal++;
      }
    }
  }
}
