package com.grahambartley.dogsunleashed.entity.variant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import org.jetbrains.annotations.Nullable;

public final class DogCoats {

  private DogCoats() {}

  public static @Nullable UnleashedDogCoat coatOf(
      final UnleashedDogBreed breed, final int coatOrdinal) {
    if (coatOrdinal < 0) {
      return null;
    }
    return switch (breed) {
      case HUSKY -> HuskyCoat.fromOrdinal(coatOrdinal);
      case DACHSHUND -> DachshundCoat.fromOrdinal(coatOrdinal);
      case BEAGLE -> BeagleCoat.fromOrdinal(coatOrdinal);
      case SHIBA_INU -> ShibaInuCoat.fromOrdinal(coatOrdinal);
      case GOLDEN_RETRIEVER -> null;
    };
  }
}
