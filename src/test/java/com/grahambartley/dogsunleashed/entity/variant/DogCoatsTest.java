package com.grahambartley.dogsunleashed.entity.variant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
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
