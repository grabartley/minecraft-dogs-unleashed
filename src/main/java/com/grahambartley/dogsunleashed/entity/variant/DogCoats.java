package com.grahambartley.dogsunleashed.entity.variant;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import java.util.List;
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
      case DIRE_WOLF -> DireWolfCoat.fromOrdinal(coatOrdinal);
      case PINSCHER -> PinscherCoat.fromOrdinal(coatOrdinal);
      case GOLDEN_RETRIEVER, CROSS_BREED -> null;
    };
  }

  public static List<UnleashedDogCoat> coatsOf(final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> List.of(HuskyCoat.values());
      case DACHSHUND -> List.of(DachshundCoat.values());
      case BEAGLE -> List.of(BeagleCoat.values());
      case SHIBA_INU -> List.of(ShibaInuCoat.values());
      case DIRE_WOLF -> List.of(DireWolfCoat.values());
      case PINSCHER -> List.of(PinscherCoat.values());
      case GOLDEN_RETRIEVER, CROSS_BREED -> List.of();
    };
  }

  public static @Nullable BiFunction<SpawnReason, Integer, UnleashedDogCoat> rollResolverFor(
      final UnleashedDogBreed breed) {
    return switch (breed) {
      case HUSKY -> HuskyCoatRolls::resolveCoatFromRoll;
      case DACHSHUND -> DachshundCoatRolls::resolveCoatFromRoll;
      case BEAGLE -> BeagleCoatRolls::resolveCoatFromRoll;
      case SHIBA_INU -> ShibaInuCoatRolls::resolveCoatFromRoll;
      case DIRE_WOLF -> DireWolfCoatRolls::resolveCoatFromRoll;
      case PINSCHER -> PinscherCoatRolls::resolveCoatFromRoll;
      case GOLDEN_RETRIEVER, CROSS_BREED -> null;
    };
  }

  public static boolean hasCoatVariants(final UnleashedDogBreed breed) {
    return rollResolverFor(breed) != null;
  }
}
