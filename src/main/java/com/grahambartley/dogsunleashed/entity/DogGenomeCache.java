package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

/**
 * Keeps a dog's genome decoded. The genome rides the data tracker as raw NBT so both logical sides
 * see it, but decoding it on every read would be wasteful, so the decoded value is cached until the
 * tracked NBT changes underneath it.
 */
public final class DogGenomeCache {

  private final UnleashedDogEntity dog;
  private @Nullable DogGenome cached;
  private boolean valid;

  DogGenomeCache(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  @Nullable
  DogGenome get() {
    if (!this.valid) {
      final NbtCompound genomeNbt = this.dog.getTrackedGenomeNbt();
      this.cached = genomeNbt.isEmpty() ? null : DogGenome.fromNbt(genomeNbt);
      this.valid = true;
    }
    return this.cached;
  }

  /** Writes the genome through to the tracker and rebases the dog's attributes on its genes. */
  void apply(final DogGenome genome) {
    this.dog.setTrackedGenomeNbt(genome.toNbt());
    this.dog
        .getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
        .setBaseValue(genome.maxHealth());
    this.dog
        .getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)
        .setBaseValue(genome.movementSpeed());
    this.dog
        .getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
        .setBaseValue(genome.attackDamage());
  }

  void invalidate() {
    this.valid = false;
  }
}
