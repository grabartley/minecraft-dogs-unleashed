package com.grahambartley.dogsunleashed.entity;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.personality.DogTraitInheritance;
import com.grahambartley.dogsunleashed.entity.personality.DogTraits;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.random.Random;

public final class DogPersonality extends DogComponent {

  private final UnleashedDogEntity dog;
  private DogTraits traits = DogTraits.NONE;

  DogPersonality(final UnleashedDogEntity dog) {
    this.dog = dog;
  }

  public DogTraits traits() {
    return this.traits;
  }

  public void set(final DogTraits traits) {
    this.traits = traits;
  }

  void rollWild() {
    final Random random = this.dog.getRandom();
    this.traits = DogTraitInheritance.wild(random::nextInt);
  }

  void inherit(final UnleashedDogEntity parentA, final UnleashedDogEntity parentB) {
    final Random random = this.dog.getRandom();
    this.traits =
        DogTraitInheritance.inherit(
            parentA.getComponents().get(DogPersonality.class).traits(),
            parentB.getComponents().get(DogPersonality.class).traits(),
            random::nextInt);
  }

  @Override
  void writeNbt(final NbtCompound nbt) {
    nbt.put(ModNbtKeys.PERSONALITY, this.traits.toNbt());
  }

  @Override
  void readNbt(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.PERSONALITY, NbtElement.LIST_TYPE)) {
      this.traits = DogTraits.fromNbt(nbt.getList(ModNbtKeys.PERSONALITY, NbtElement.STRING_TYPE));
    } else {
      this.rollWild();
    }
  }
}
