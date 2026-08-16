package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import net.minecraft.text.Text;

final class DogBreedNames {

  private static final String MIX_NAME_KEY = "entity.dogs-unleashed.crossbreed.mix";

  private DogBreedNames() {}

  static Text displayName(final UnleashedDogBreed breed, final List<BreedShare> composition) {
    if (breed != UnleashedDogBreed.CROSS_BREED) {
      return Text.translatable(breed.translationKey());
    }
    final List<UnleashedDogBreed> mixBreeds = DogTraitFormat.mixNameBreeds(composition);
    if (mixBreeds.isEmpty()) {
      return Text.translatable(breed.translationKey());
    }
    return Text.translatable(
        MIX_NAME_KEY,
        Text.translatable(mixBreeds.get(0).translationKey()),
        Text.translatable(mixBreeds.get(1).translationKey()));
  }
}
