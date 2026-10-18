package com.grahambartley.dogsunleashed.network.payload;

import static com.grahambartley.dogsunleashed.network.PacketLimits.CARRIED_ALLELES_MAX_SIZE;
import static com.grahambartley.dogsunleashed.network.PacketLimits.GENES_MAX_SIZE;

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
    final List<GeneSyncData> written = genes.subList(0, Math.min(genes.size(), GENES_MAX_SIZE));
    buf.writeVarInt(written.size());
    for (final GeneSyncData gene : written) {
      buf.writeString(gene.locus.breed().serializedId());
      buf.writeVarInt(gene.locus.trait().ordinal());
      buf.writeVarInt(gene.shown);
      final List<Integer> carried =
          gene.carried.subList(0, Math.min(gene.carried.size(), CARRIED_ALLELES_MAX_SIZE));
      buf.writeVarInt(carried.size());
      for (final int allele : carried) {
        buf.writeVarInt(allele);
      }
    }
  }

  static List<GeneSyncData> readList(final RegistryByteBuf buf) {
    final int size = Math.min(buf.readVarInt(), GENES_MAX_SIZE);
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
    final int carriedCount = Math.min(buf.readVarInt(), CARRIED_ALLELES_MAX_SIZE);
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
