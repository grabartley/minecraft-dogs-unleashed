package com.grahambartley.dogsunleashed.entity.genome;

import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public record DogGenes(Map<DogGeneLocus, AllelePair> pairs) {

  private static final int ALLELES_PER_GENE = 2;

  public DogGenes {
    pairs = Map.copyOf(pairs);
  }

  public static Set<DogGeneLocus> lociFor(
      final UnleashedDogBreed rigBreed, final List<BreedShare> composition) {
    final Set<DogGeneLocus> loci = new LinkedHashSet<>(DogGeneLocus.lociOf(rigBreed));
    for (final BreedShare share : composition) {
      loci.addAll(DogGeneLocus.lociOf(share.breed()));
    }
    return loci;
  }

  public static DogGenes matching(final DogAppearance appearance, final Set<DogGeneLocus> loci) {
    final Map<DogGeneLocus, AllelePair> pairs = new LinkedHashMap<>();
    for (final DogGeneLocus locus : loci) {
      final GeneDominance dominance = locus.dominance();
      final int shown = locus.trait().shownIn(appearance);
      final boolean visible =
          locus.breed() == appearance.rigSourceBreed() && dominance.isValid(shown);
      pairs.put(locus, AllelePair.matching(visible ? shown : dominance.wildType()));
    }
    return new DogGenes(pairs);
  }

  public static DogGenes wild(
      final Set<DogGeneLocus> loci, final SpawnReason spawnReason, final IntUnaryOperator nextInt) {
    final Map<DogGeneLocus, AllelePair> pairs = new LinkedHashMap<>();
    for (final DogGeneLocus locus : loci) {
      final DogGeneTrait trait = locus.trait();
      pairs.put(
          locus,
          DogGeneInheritance.wild(
              trait.rollShown(locus.breed(), spawnReason, nextInt),
              locus.dominance(),
              () -> trait.rollCarried(locus.breed(), nextInt)));
    }
    return new DogGenes(pairs);
  }

  public static DogGenes inherit(
      final DogGenes parentA,
      final DogGenes parentB,
      final Set<DogGeneLocus> loci,
      final IntUnaryOperator nextInt) {
    final Map<DogGeneLocus, AllelePair> pairs = new LinkedHashMap<>();
    for (final DogGeneLocus locus : loci) {
      pairs.put(
          locus,
          DogGeneInheritance.inherit(
              parentA.pair(locus), parentB.pair(locus), locus.dominance(), nextInt));
    }
    return new DogGenes(pairs);
  }

  public @Nullable AllelePair pair(final DogGeneLocus locus) {
    return this.pairs.get(locus);
  }

  public DogAppearance expressOnto(final DogAppearance appearance) {
    DogAppearance expressed = appearance;
    for (final DogGeneLocus locus : DogGeneLocus.lociOf(appearance.rigSourceBreed())) {
      final AllelePair pair = this.pair(locus);
      if (pair != null) {
        expressed = locus.trait().withShown(expressed, locus.dominance().express(pair));
      }
    }
    return expressed;
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    for (final Map.Entry<DogGeneLocus, AllelePair> entry : this.pairs.entrySet()) {
      final DogGeneLocus locus = entry.getKey();
      final String breedId = locus.breed().serializedId();
      final NbtCompound breedGenes = nbt.getCompound(breedId);
      breedGenes.putIntArray(
          locus.trait().serializedId(),
          new int[] {entry.getValue().first(), entry.getValue().second()});
      nbt.put(breedId, breedGenes);
    }
    return nbt;
  }

  public static DogGenes fromNbt(final NbtCompound nbt) {
    final Map<DogGeneLocus, AllelePair> pairs = new LinkedHashMap<>();
    for (final String breedId : nbt.getKeys()) {
      final UnleashedDogBreed breed = UnleashedDogBreed.fromSerializedIdOrNull(breedId);
      if (breed == null || !nbt.contains(breedId, NbtElement.COMPOUND_TYPE)) {
        continue;
      }
      final NbtCompound breedGenes = nbt.getCompound(breedId);
      for (final DogGeneLocus locus : DogGeneLocus.lociOf(breed)) {
        final int[] alleles = breedGenes.getIntArray(locus.trait().serializedId());
        final GeneDominance dominance = locus.dominance();
        if (alleles.length == ALLELES_PER_GENE
            && dominance.isValid(alleles[0])
            && dominance.isValid(alleles[1])) {
          pairs.put(locus, new AllelePair(alleles[0], alleles[1]));
        }
      }
    }
    return new DogGenes(pairs);
  }
}
