package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.PetData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.Nullable;

public record DogStatueLook(
    UnleashedDogBreed breed,
    boolean baby,
    int coatVariant,
    int eyeColorVariant,
    int furStage,
    int growthStage,
    @Nullable DogGenome genome,
    @Nullable DogGenes genes) {

  public static final Codec<DogStatueLook> CODEC =
      NbtCompound.CODEC.comapFlatMap(
          nbt -> {
            final DogStatueLook look = fromNbt(nbt);
            return look == null
                ? DataResult.error(() -> "A dog statue needs a known breed")
                : DataResult.success(look);
          },
          DogStatueLook::toNbt);

  public static DogStatueLook of(final UnleashedDogEntity dog) {
    return new DogStatueLook(
        dog.getBreed(),
        dog.isBaby(),
        PetData.coatVariantOf(dog),
        PetData.huskyEyeVariantOf(dog),
        dog.getFur().stage().ordinal(),
        dog.getGrowth().stage().number(),
        dog.getGenome(),
        dog.getGenes().genes());
  }

  public static DogStatueLook of(final PetData pet) {
    return new DogStatueLook(
        pet.getBreed(),
        pet.isBaby(),
        pet.getCoatVariant(),
        pet.getHuskyEyeVariant(),
        pet.getFurStage(),
        pet.getGrowthStage(),
        pet.getGenome(),
        pet.getGenes());
  }

  public void applyTo(final UnleashedDogEntity dog) {
    dog.readCustomDataFromNbt(this.toNbt());
    dog.setBaby(this.baby);
  }

  public UnleashedDogBreed rigBreed() {
    return DogGenome.rigBreedOf(
        this.breed, this.genome == null ? List.of() : this.genome.composition());
  }

  public boolean howls() {
    return this.rigBreed().howls();
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(ModNbtKeys.BREED_TYPE, this.breed.serializedId());
    nbt.putBoolean(ModNbtKeys.PORTRAIT_BABY, this.baby);
    if (this.coatVariant >= 0) {
      nbt.putInt(ModNbtKeys.COAT_VARIANT, this.coatVariant);
    }
    if (this.eyeColorVariant >= 0) {
      nbt.putInt(ModNbtKeys.EYE_COLOR_VARIANT, this.eyeColorVariant);
    }
    nbt.putInt(ModNbtKeys.FUR_STAGE, this.furStage);
    nbt.putInt(ModNbtKeys.GROWTH_STAGE, this.growthStage);
    if (this.genome != null) {
      nbt.put(ModNbtKeys.GENOME, this.genome.toNbt());
    }
    if (this.genes != null) {
      nbt.put(ModNbtKeys.GENES, this.genes.toNbt());
    }
    return nbt;
  }

  public static @Nullable DogStatueLook fromNbt(final NbtCompound nbt) {
    final UnleashedDogBreed breed =
        UnleashedDogBreed.fromSerializedIdOrNull(nbt.getString(ModNbtKeys.BREED_TYPE));
    if (breed == null) {
      return null;
    }
    return new DogStatueLook(
        breed,
        nbt.getBoolean(ModNbtKeys.PORTRAIT_BABY),
        intOrUnset(nbt, ModNbtKeys.COAT_VARIANT),
        intOrUnset(nbt, ModNbtKeys.EYE_COLOR_VARIANT),
        nbt.getInt(ModNbtKeys.FUR_STAGE),
        nbt.getInt(ModNbtKeys.GROWTH_STAGE),
        nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)
            ? DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME))
            : null,
        nbt.contains(ModNbtKeys.GENES, NbtElement.COMPOUND_TYPE)
            ? DogGenes.fromNbt(nbt.getCompound(ModNbtKeys.GENES))
            : null);
  }

  private static int intOrUnset(final NbtCompound nbt, final String key) {
    return nbt.contains(key, NbtElement.NUMBER_TYPE)
        ? nbt.getInt(key)
        : UnleashedDogEntity.UNSET_VARIANT;
  }
}
