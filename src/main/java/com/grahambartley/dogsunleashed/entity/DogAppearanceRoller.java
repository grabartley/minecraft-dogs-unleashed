package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.genome.DogGenomeCombiner;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.entity.variant.UnleashedDogCoat;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntSupplier;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.random.Random;

public final class DogAppearanceRoller {

  private final UnleashedDogEntity dog;

  DogAppearanceRoller(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static List<UnleashedDogBreed> naturalFounders() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(UnleashedDogBreed::isNaturallySpawning)
        .toList();
  }

  /**
   * The second founder is drawn from a range one shorter and then shifted past the first, which
   * lands on a distinct partner in a single draw instead of rerolling until the two differ.
   */
  public static int distinctSecondIndex(final int firstIndex, final int rawSecondIndex) {
    return rawSecondIndex >= firstIndex ? rawSecondIndex + 1 : rawSecondIndex;
  }

  /**
   * The roll is a supplier because the original only reaches for the entity RNG when the breed has
   * something to roll, and drawing an extra number would shift every later roll in the same tick.
   */
  public static int resolvedVariantOrdinal(
      final boolean rollable, final int currentOrdinal, final IntSupplier roll) {
    return rollable ? roll.getAsInt() : currentOrdinal;
  }

  void rollAppearance(final SpawnReason spawnReason) {
    final UnleashedDogBreed rigBreed = this.dog.getRigSourceBreed();
    final BiFunction<SpawnReason, Integer, UnleashedDogCoat> rollResolver =
        DogCoats.rollResolverFor(rigBreed);
    final Random random = this.dog.getRandom();
    final DogTraits current = this.dog.getTraits();
    final int coatVariantOrdinal =
        resolvedVariantOrdinal(
            rollResolver != null,
            current.coatVariantOrdinal(),
            () ->
                rollResolver.apply(spawnReason, random.nextInt(DogCoats.ROLL_BOUND)).getOrdinal());
    final int eyeColorVariantOrdinal =
        resolvedVariantOrdinal(
            rigBreed.hasEyeColorVariants(),
            current.eyeColorVariantOrdinal(),
            () -> HuskyEyeColor.fromRandom(random).ordinal());
    this.dog.applyTraits(new DogTraits(rigBreed, coatVariantOrdinal, eyeColorVariantOrdinal));
  }

  DogGenome genomeOrPure() {
    final DogGenome genome = this.dog.getGenome();
    return genome != null ? genome : DogGenome.pure(this.dog.getBreed());
  }

  DogGenome randomFounderMix() {
    final List<UnleashedDogBreed> founders = naturalFounders();
    final Random random = this.dog.getRandom();
    final int firstIndex = random.nextInt(founders.size());
    final int secondIndex = distinctSecondIndex(firstIndex, random.nextInt(founders.size() - 1));
    return DogGenomeCombiner.combine(
        DogGenome.pure(founders.get(firstIndex)),
        DogGenome.pure(founders.get(secondIndex)),
        random);
  }
}
