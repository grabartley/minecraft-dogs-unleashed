package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.genome.DogGenomeCombiner;
import java.util.Arrays;
import java.util.List;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.random.Random;

public final class DogAppearanceRoller {

  private final UnleashedDogEntity dog;

  DogAppearanceRoller(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public static List<UnleashedDogBreed> naturalFounders() {
    return Arrays.stream(UnleashedDogBreed.values())
        .filter(UnleashedDogBreed::isCrossBreedFounder)
        .toList();
  }

  public static int distinctSecondIndex(final int firstIndex, final int rawSecondIndex) {
    return rawSecondIndex >= firstIndex ? rawSecondIndex + 1 : rawSecondIndex;
  }

  void rollAppearance(final SpawnReason spawnReason) {
    final Random random = this.dog.getRandom();
    applyGenes(DogGenes.wild(this.dog.getGenes().loci(), spawnReason, random::nextInt));
  }

  void inheritAppearance(final UnleashedDogEntity parentA, final UnleashedDogEntity parentB) {
    final Random random = this.dog.getRandom();
    applyGenes(
        DogGenes.inherit(
            parentA.getGenes().genes(),
            parentB.getGenes().genes(),
            this.dog.getGenes().loci(),
            random::nextInt));
  }

  private void applyGenes(final DogGenes genes) {
    this.dog.getGenes().set(genes);
    final DogAppearance current = this.dog.getAppearance();
    this.dog.applyAppearance(
        genes.expressOnto(
            new DogAppearance(
                this.dog.getRigSourceBreed(),
                current.coatVariantOrdinal(),
                current.eyeColorVariantOrdinal())));
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
