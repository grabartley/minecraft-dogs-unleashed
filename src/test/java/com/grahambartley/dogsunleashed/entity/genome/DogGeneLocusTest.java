package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGeneLocusTest {

  static Stream<Arguments> lociPerBreed() {
    return Stream.of(
        Arguments.of(UnleashedDogBreed.HUSKY, List.of(DogGeneTrait.COAT, DogGeneTrait.EYES)),
        Arguments.of(UnleashedDogBreed.BEAGLE, List.of(DogGeneTrait.COAT)),
        Arguments.of(UnleashedDogBreed.DIRE_WOLF, List.of(DogGeneTrait.COAT)),
        Arguments.of(UnleashedDogBreed.GOLDEN_RETRIEVER, List.of()),
        Arguments.of(UnleashedDogBreed.CROSS_BREED, List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("lociPerBreed")
  @DisplayName("a breed has one gene per trait it can vary in")
  void lociFollowTheBreedsVariants(final UnleashedDogBreed breed, final List<DogGeneTrait> traits) {
    assertEquals(
        traits.stream().map(trait -> new DogGeneLocus(breed, trait)).toList(),
        DogGeneLocus.lociOf(breed));
  }

  @Test
  @DisplayName("a locus reads dominance and names from its breed and trait")
  void locusDelegatesToItsTrait() {
    final DogGeneLocus locus = new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
    assertEquals(DogGeneTrait.COAT.dominance(UnleashedDogBreed.HUSKY), locus.dominance());
    assertEquals(
        HuskyCoat.WHITE.translationKey(), locus.alleleTranslationKey(HuskyCoat.WHITE.ordinal()));
  }
}
