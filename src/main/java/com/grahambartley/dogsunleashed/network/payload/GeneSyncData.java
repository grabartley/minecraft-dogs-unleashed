package com.grahambartley.dogsunleashed.network.payload;

import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.genome.AllelePair;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneTrait;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.GeneDominance;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import org.jetbrains.annotations.Nullable;

public record GeneSyncData(DogGeneLocus locus, int shown, List<Integer> carried) {

  static final int MAX_GENES = DogGeneTrait.values().length;
  static final int MAX_CARRIED = 2;

  public GeneSyncData {
    carried = List.copyOf(carried);
  }

  public boolean carrier() {
    return !this.carried.isEmpty();
  }

  public static List<GeneSyncData> visibleGenes(
      final DogGenes genes, final UnleashedDogBreed rigBreed, final boolean revealCarried) {
    final List<GeneSyncData> visible = new ArrayList<>();
    for (final DogGeneLocus locus : DogGeneLocus.lociOf(rigBreed)) {
      final AllelePair pair = genes.pair(locus);
      if (pair != null) {
        final GeneDominance dominance = locus.dominance();
        visible.add(
            new GeneSyncData(
                locus,
                dominance.express(pair),
                revealCarried ? dominance.carried(pair) : List.of()));
      }
    }
    return visible;
  }

  static void writeList(final RegistryByteBuf buf, final List<GeneSyncData> genes) {
    buf.writeVarInt(genes.size());
    for (final GeneSyncData gene : genes) {
      buf.writeString(gene.locus.breed().serializedId());
      buf.writeVarInt(gene.locus.trait().ordinal());
      buf.writeVarInt(gene.shown);
      buf.writeVarInt(gene.carried.size());
      for (final int allele : gene.carried) {
        buf.writeVarInt(allele);
      }
    }
  }

  static List<GeneSyncData> readList(final RegistryByteBuf buf) {
    final int size = buf.readVarInt();
    if (size < 0 || size > MAX_GENES) {
      throw new IllegalArgumentException("Too many genes: " + size);
    }
    final List<GeneSyncData> genes = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      final GeneSyncData gene = read(buf);
      if (gene != null) {
        genes.add(gene);
      }
    }
    return genes;
  }

  private static @Nullable GeneSyncData read(final RegistryByteBuf buf) {
    final UnleashedDogBreed breed = UnleashedDogBreed.fromSerializedIdOrNull(buf.readString());
    final int traitOrdinal = buf.readVarInt();
    final int shown = buf.readVarInt();
    final int carriedCount = buf.readVarInt();
    if (carriedCount < 0 || carriedCount > MAX_CARRIED) {
      throw new IllegalArgumentException("Too many carried alleles: " + carriedCount);
    }
    final List<Integer> carried = new ArrayList<>(carriedCount);
    for (int i = 0; i < carriedCount; i++) {
      carried.add(buf.readVarInt());
    }
    if (breed == null || traitOrdinal < 0 || traitOrdinal >= DogGeneTrait.values().length) {
      return null;
    }
    final DogGeneTrait trait = DogGeneTrait.values()[traitOrdinal];
    if (!trait.appliesTo(breed)) {
      return null;
    }
    final DogGeneLocus locus = new DogGeneLocus(breed, trait);
    final GeneDominance dominance = locus.dominance();
    if (!dominance.isValid(shown) || !carried.stream().allMatch(dominance::isValid)) {
      return null;
    }
    return new GeneSyncData(locus, shown, carried);
  }
}
