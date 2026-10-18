package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.genome.DogGeneLocus;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import java.util.List;
import java.util.Set;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public final class DogGeneHolder {

  private final UnleashedDogEntity dog;
  private @Nullable DogGenes stored;

  DogGeneHolder(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public DogGenes genes() {
    return this.stored != null ? this.stored : DogGenes.matching(this.dog.getAppearance(), loci());
  }

  public void set(final DogGenes genes) {
    this.stored = genes;
  }

  Set<DogGeneLocus> loci() {
    final DogGenome genome = this.dog.getGenome();
    return DogGenes.lociFor(
        this.dog.getRigSourceBreed(), genome != null ? genome.composition() : List.of());
  }

  void writeNbt(final NbtCompound nbt) {
    nbt.put(ModNbtKeys.GENES, this.genes().toNbt());
  }

  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.GENES, NbtElement.COMPOUND_TYPE)) {
      this.stored = DogGenes.fromNbt(nbt.getCompound(ModNbtKeys.GENES));
    }
  }
}
