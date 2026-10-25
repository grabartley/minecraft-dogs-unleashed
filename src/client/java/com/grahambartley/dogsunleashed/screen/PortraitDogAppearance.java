package com.grahambartley.dogsunleashed.screen;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.network.payload.PetSyncData;
import com.grahambartley.dogsunleashed.pet.PortraitLook;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfits;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

record PortraitDogAppearance(
    UnleashedDogBreed entityBreed,
    boolean undead,
    UUID entityId,
    @Nullable DogGenome genome,
    int eyeColorVariant,
    PortraitLook look) {

  static PortraitDogAppearance of(final PetSyncData pet) {
    return new PortraitDogAppearance(
        pet.breed(),
        pet.undead(),
        UUID.fromString(pet.petId()),
        genomeOf(pet),
        pet.displayBreed().hasEyeColorVariants()
            ? pet.huskyEyeVariant()
            : UnleashedDogEntity.UNSET_VARIANT,
        pet.look());
  }

  void applyTo(final UnleashedDogEntity dog) {
    dog.setBaby(this.look.baby());
    dog.readCustomDataFromNbt(this.toNbt());
    DogPortraitOutfits.dress(dog, this.look.outfit());
  }

  NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    if (this.genome != null) {
      nbt.put(ModNbtKeys.GENOME, this.genome.toNbt());
    }
    if (this.look.coatVariant() >= 0) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, this.look.coatVariant());
    }
    if (this.eyeColorVariant >= 0) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, this.eyeColorVariant);
    }
    nbt.putInt(ModNbtKeys.FUR_STAGE, this.look.furStage());
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, this.look.growthStage());
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
        DogGenome.BASELINE_FUR_GROWTH_RATE,
        DogGenome.dominantOf(pet.composition()));
  }
}
