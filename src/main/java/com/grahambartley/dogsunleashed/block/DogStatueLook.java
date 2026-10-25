package com.grahambartley.dogsunleashed.block;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.DogAppearance;
import com.grahambartley.dogsunleashed.entity.DogEntityNbt;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.entity.variant.HuskyEyeColor;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
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

  public static final PacketCodec<RegistryByteBuf, DogStatueLook> PACKET_CODEC =
      PacketCodecs.registryCodec(CODEC);

  public static DogStatueLook of(final UnleashedDogEntity dog) {
    return new DogStatueLook(
        dog.getBreed(),
        dog.isBaby(),
        coatVariantOf(dog),
        eyeColorVariantOf(dog),
        dog.getFur().stage().ordinal(),
        dog.getGrowth().stage().number(),
        dog.getGenome(),
        dog.getGenes().genes());
  }

  public static DogStatueLook unseen(final UnleashedDogBreed breed) {
    return new DogStatueLook(
        breed,
        false,
        UnleashedDogEntity.UNSET_VARIANT,
        UnleashedDogEntity.UNSET_VARIANT,
        DogFurStage.NATURAL.ordinal(),
        0,
        null,
        null);
  }

  public List<BreedShare> composition() {
    return this.genome == null ? List.of() : this.genome.composition();
  }

  public DogGenes resolvedGenes() {
    if (this.genes != null) {
      return this.genes;
    }
    final UnleashedDogBreed rigBreed = this.rigBreed();
    return DogGenes.matching(
        new DogAppearance(rigBreed, this.coatVariant, this.eyeColorVariant),
        DogGenes.lociFor(rigBreed, this.composition()));
  }

  public DogStatueLook withGenes(final @Nullable DogGenes genes) {
    return new DogStatueLook(
        this.breed,
        this.baby,
        this.coatVariant,
        this.eyeColorVariant,
        this.furStage,
        this.growthStage,
        this.genome,
        genes);
  }

  public DogStatueLook withEyeColorVariant(final int eyeColorVariant) {
    return new DogStatueLook(
        this.breed,
        this.baby,
        this.coatVariant,
        eyeColorVariant,
        this.furStage,
        this.growthStage,
        this.genome,
        this.genes);
  }

  public void applyTo(final UnleashedDogEntity dog) {
    dog.readCustomDataFromNbt(this.toNbt());
    dog.setBaby(this.baby);
  }

  public UnleashedDogBreed rigBreed() {
    return DogGenome.rigBreedOf(this.breed, this.composition());
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
        DogEntityNbt.variantOrdinalOr(
            nbt, ModNbtKeys.COAT_VARIANT, UnleashedDogEntity.UNSET_VARIANT),
        DogEntityNbt.variantOrdinalOr(
            nbt, ModNbtKeys.EYE_COLOR_VARIANT, UnleashedDogEntity.UNSET_VARIANT),
        nbt.getInt(ModNbtKeys.FUR_STAGE),
        nbt.getInt(ModNbtKeys.GROWTH_STAGE),
        nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)
            ? DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME))
            : null,
        nbt.contains(ModNbtKeys.GENES, NbtElement.COMPOUND_TYPE)
            ? DogGenes.fromNbt(nbt.getCompound(ModNbtKeys.GENES))
            : null);
  }

  private static int coatVariantOf(final UnleashedDogEntity dog) {
    return dog.getCoatVariant() != null
        ? dog.getCoatVariant().getOrdinal()
        : UnleashedDogEntity.UNSET_VARIANT;
  }

  private static int eyeColorVariantOf(final UnleashedDogEntity dog) {
    final HuskyEyeColor eyeColor = dog.getEyeColorVariant();
    return eyeColor != null ? eyeColor.ordinal() : UnleashedDogEntity.UNSET_VARIANT;
  }
}
