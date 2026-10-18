package com.grahambartley.dogsunleashed.entity.genome;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.dogsunleashed.MinecraftBootstrapExtension;
import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.variant.BeagleCoat;
import com.grahambartley.dogsunleashed.entity.variant.DogCoats;
import com.grahambartley.dogsunleashed.entity.variant.HuskyCoat;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class DogGenesTest {

  private static final DogGeneLocus HUSKY_COAT =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.COAT);
  private static final DogGeneLocus HUSKY_EYES =
      new DogGeneLocus(UnleashedDogBreed.HUSKY, DogGeneTrait.EYES);
  private static final DogGeneLocus BEAGLE_COAT =
      new DogGeneLocus(UnleashedDogBreed.BEAGLE, DogGeneTrait.COAT);

  @Test
  @DisplayName("a cross carries the genes of its rig breed and every breed in its mix")
  void lociCoverRigAndComposition() {
    assertEquals(
        Set.of(HUSKY_COAT, HUSKY_EYES, BEAGLE_COAT),
        DogGenes.lociFor(
            UnleashedDogBreed.HUSKY,
            List.of(
                new BreedShare(UnleashedDogBreed.HUSKY, 0.5f),
                new BreedShare(UnleashedDogBreed.BEAGLE, 0.3f),
                new BreedShare(UnleashedDogBreed.GOLDEN_RETRIEVER, 0.2f))));
  }

  @Test
  @DisplayName("genes made from a look carry two copies of what shows and wild type elsewhere")
  void matchingCopiesTheLook() {
    final DogGenes genes =
        DogGenes.matching(
            new DogAppearance(
                UnleashedDogBreed.HUSKY,
                HuskyCoat.WHITE.ordinal(),
                HuskyEyeColor.BLUE_HAZEL.ordinal()),
            Set.of(HUSKY_COAT, HUSKY_EYES, BEAGLE_COAT));
    assertEquals(AllelePair.matching(HuskyCoat.WHITE.ordinal()), genes.pair(HUSKY_COAT));
    assertEquals(AllelePair.matching(HuskyEyeColor.BLUE_HAZEL.ordinal()), genes.pair(HUSKY_EYES));
    assertEquals(AllelePair.matching(BEAGLE_COAT.dominance().wildType()), genes.pair(BEAGLE_COAT));
  }

  @Test
  @DisplayName("genes made from an unset look fall back to the wild type")
  void matchingAnUnsetLookUsesWildType() {
    final DogGenes genes =
        DogGenes.matching(new DogAppearance(UnleashedDogBreed.HUSKY, -1, -1), Set.of(HUSKY_COAT));
    assertEquals(AllelePair.matching(HUSKY_COAT.dominance().wildType()), genes.pair(HUSKY_COAT));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = UnleashedDogBreed.class,
      names = {"HUSKY", "DACHSHUND", "BEAGLE", "SHIBA_INU", "DIRE_WOLF"})
  @DisplayName("wild genes show exactly the coat the spawn table rolled")
  void wildGenesShowTheRolledCoat(final UnleashedDogBreed breed) {
    final DogGeneLocus coat = new DogGeneLocus(breed, DogGeneTrait.COAT);
    for (int shownRoll = 0; shownRoll < DogCoats.ROLL_BOUND; shownRoll++) {
      for (int carriedRoll = 0; carriedRoll < DogCoats.ROLL_BOUND; carriedRoll += 7) {
        final int[] rolls = {shownRoll, carriedRoll};
        final int[] next = {0};
        final DogGenes genes =
            DogGenes.wild(Set.of(coat), SpawnReason.NATURAL, bound -> rolls[next[0]++ % 2]);
        final int rolled =
            DogCoats.rollResolverFor(breed).apply(SpawnReason.NATURAL, shownRoll).getOrdinal();
        assertEquals(rolled, coat.dominance().express(genes.pair(coat)));
      }
    }
  }

  @Test
  @DisplayName("a pup's genes come from both parents gene by gene")
  void inheritTakesFromBothParents() {
    final DogGenes parentA =
        new DogGenes(Map.of(HUSKY_COAT, AllelePair.matching(HuskyCoat.SABLE.ordinal())));
    final DogGenes parentB =
        new DogGenes(Map.of(HUSKY_COAT, AllelePair.matching(HuskyCoat.WHITE.ordinal())));
    final DogGenes pup =
        DogGenes.inherit(parentA, parentB, Set.of(HUSKY_COAT), bound -> bound == 32 ? 1 : 0);
    assertEquals(
        new AllelePair(HuskyCoat.SABLE.ordinal(), HuskyCoat.WHITE.ordinal()), pup.pair(HUSKY_COAT));
  }

  @Test
  @DisplayName("the rig breed's genes decide the look and other breeds' genes stay hidden")
  void expressOntoChangesOnlyTheRigBreed() {
    final DogGenes genes =
        new DogGenes(
            Map.of(
                HUSKY_COAT,
                new AllelePair(HuskyCoat.WHITE.ordinal(), HuskyCoat.GREY_WHITE.ordinal()),
                BEAGLE_COAT,
                AllelePair.matching(BeagleCoat.LILAC_2.ordinal())));
    assertEquals(
        new DogAppearance(UnleashedDogBreed.HUSKY, HuskyCoat.GREY_WHITE.ordinal(), 3),
        genes.expressOnto(new DogAppearance(UnleashedDogBreed.HUSKY, 0, 3)));
  }

  @Test
  @DisplayName("genes survive an NBT round trip")
  void nbtRoundTrip() {
    final DogGenes genes =
        new DogGenes(
            Map.of(
                HUSKY_COAT,
                new AllelePair(HuskyCoat.BLACK_WHITE.ordinal(), HuskyCoat.WHITE.ordinal()),
                HUSKY_EYES,
                new AllelePair(0, 2),
                BEAGLE_COAT,
                new AllelePair(1, 11)));
    assertEquals(genes, DogGenes.fromNbt(genes.toNbt()));
  }

  @Test
  @DisplayName("unknown breeds, unknown traits and out of range alleles are dropped on load")
  void fromNbtDropsInvalidEntries() {
    final NbtCompound nbt = new NbtCompound();
    final NbtCompound husky = new NbtCompound();
    husky.putIntArray("coat", new int[] {0, 99});
    husky.putIntArray("eyes", new int[] {1, 1});
    husky.putIntArray("tail", new int[] {0, 0});
    nbt.put(UnleashedDogBreed.HUSKY.serializedId(), husky);
    final NbtCompound unknown = new NbtCompound();
    unknown.putIntArray("coat", new int[] {0, 0});
    nbt.put("poodle", unknown);
    final NbtCompound beagle = new NbtCompound();
    beagle.putIntArray("coat", new int[] {0});
    nbt.put(UnleashedDogBreed.BEAGLE.serializedId(), beagle);

    final DogGenes genes = DogGenes.fromNbt(nbt);
    assertEquals(Map.of(HUSKY_EYES, AllelePair.matching(1)), genes.pairs());
    assertTrue(genes.pair(HUSKY_COAT) == null);
  }
}
