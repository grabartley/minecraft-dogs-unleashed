package com.grahambartley.dogsunleashed.entity.variant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.function.BiFunction;
import net.minecraft.entity.SpawnReason;
import org.jetbrains.annotations.Nullable;

public final class DogCoats {

  public static final int ROLL_BOUND = 100;

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

  public static @Nullable BiFunction<SpawnReason, Integer, UnleashedDogCoat> rollResolverFor(
      final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> HuskyCoatRolls::resolveCoatFromRoll;
      case DACHSHUND -> DachshundCoatRolls::resolveCoatFromRoll;
      case BEAGLE -> BeagleCoatRolls::resolveCoatFromRoll;
      case SHIBA_INU -> ShibaInuCoatRolls::resolveCoatFromRoll;
      case GOLDEN_RETRIEVER -> null;
    };
  }

  public static boolean hasCoatVariants(final UnleashedDogBreed breed) {
    return rollResolverFor(breed) != null;
  }
}
