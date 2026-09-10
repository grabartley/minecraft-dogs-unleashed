package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

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
