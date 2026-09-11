package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

record PortraitDogAppearance(
    UnleashedDogBreed entityBreed,
    boolean undead,
    UUID entityId,
    boolean baby,
    DyeColor collarColor,
    @Nullable DogGenome genome,
    int coatVariant,
    int eyeColorVariant) {

  private static final int COLLAR_COLOR_COUNT = DyeColor.values().length;

  static PortraitDogAppearance of(final PetSyncData pet) {
    return new PortraitDogAppearance(
        pet.breed(),
        pet.undead(),
        UUID.fromString(pet.petId()),
        pet.baby(),
        DyeColor.byId(Math.floorMod(pet.collarColor(), COLLAR_COLOR_COUNT)),
        genomeOf(pet),
        pet.coatVariant(),
        pet.displayBreed().hasEyeColorVariants()
            ? pet.huskyEyeVariant()
            : UnleashedDogEntity.UNSET_VARIANT);
  }

  void applyTo(final UnleashedDogEntity dog) {
    dog.setBaby(this.baby);
    dog.setCollarColor(this.collarColor);
    dog.readCustomDataFromNbt(this.toNbt());
  }

  NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    if (this.genome != null) {
      nbt.put(ModNbtKeys.GENOME, this.genome.toNbt());
    }
    if (this.coatVariant >= 0) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, this.coatVariant);
    }
    if (this.eyeColorVariant >= 0) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, this.eyeColorVariant);
    }
    return nbt;
  }

  private static @Nullable DogGenome genomeOf(final PetSyncData pet) {
    if (pet.composition().isEmpty()) {
      return null;
    }
    return new DogGenome(
        pet.composition(),
        pet.maxHealth(),
        pet.movementSpeed(),
        pet.attackDamage(),
        DogGenome.dominantOf(pet.composition()));
  }
}
