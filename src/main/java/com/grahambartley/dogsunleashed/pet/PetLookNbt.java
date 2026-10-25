package com.grahambartley.dogsunleashed.pet;

import com.grahambartley.dogsunleashed.DogsUnleashed;
import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.block.DogStatueLook;
import com.grahambartley.dogsunleashed.entity.DogEntityNbt;
import com.grahambartley.dogsunleashed.entity.DogFurStage;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.entity.UnleashedDogEntity;
import com.grahambartley.dogsunleashed.entity.genome.DogGenes;
import com.grahambartley.dogsunleashed.entity.genome.DogGenome;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import com.grahambartley.dogsunleashed.portrait.DogPortraitOutfit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.Nullable;

final class PetLookNbt {

  private PetLookNbt() {}

  static void write(
      final NbtCompound nbt,
      final DogStatueLook look,
      final DogPortraitOutfit outfit,
      final RegistryWrapper.WrapperLookup registries) {
    nbt.put(ModNbtKeys.PET_LOOK, look.toNbt());
    DogPortraitOutfit.CODEC
        .encodeStart(registries.getOps(NbtOps.INSTANCE), outfit)
        .resultOrPartial(DogsUnleashed.log::warn)
        .ifPresent(saved -> nbt.put(ModNbtKeys.PORTRAIT_OUTFIT, saved));
  }

  static DogStatueLook readLook(final NbtCompound nbt) {
    final DogStatueLook saved =
        nbt.contains(ModNbtKeys.PET_LOOK, NbtElement.COMPOUND_TYPE)
            ? DogStatueLook.fromNbt(nbt.getCompound(ModNbtKeys.PET_LOOK))
            : null;
    return saved != null ? saved : legacyLook(nbt);
  }

  static DogPortraitOutfit readOutfit(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    if (nbt.contains(ModNbtKeys.PORTRAIT_OUTFIT, NbtElement.COMPOUND_TYPE)) {
      return DogPortraitOutfit.CODEC
          .parse(registries.getOps(NbtOps.INSTANCE), nbt.get(ModNbtKeys.PORTRAIT_OUTFIT))
          .resultOrPartial(DogsUnleashed.log::warn)
          .orElse(DogPortraitOutfit.NONE);
    }
    return new DogPortraitOutfit(
        Map.of(),
        nbt.contains(ModNbtKeys.PORTRAIT_COLLAR, NbtElement.NUMBER_TYPE)
            ? Optional.of(DyeColor.byId(nbt.getInt(ModNbtKeys.PORTRAIT_COLLAR)))
            : Optional.empty(),
        ItemStack.EMPTY,
        Optional.empty(),
        false);
  }

  private static DogStatueLook legacyLook(final NbtCompound nbt) {
    final UnleashedDogBreed breed =
        UnleashedDogBreed.fromSerializedId(nbt.getString(ModNbtKeys.BREED_TYPE));
    return new DogStatueLook(
        breed,
        nbt.getBoolean(ModNbtKeys.PORTRAIT_BABY),
        DogEntityNbt.variantOrdinalOr(
            nbt, ModNbtKeys.PORTRAIT_COAT_VARIANT, UnleashedDogEntity.UNSET_VARIANT),
        DogEntityNbt.variantOrdinalOr(
            nbt, ModNbtKeys.PORTRAIT_HUSKY_EYE, UnleashedDogEntity.UNSET_VARIANT),
        nbt.contains(ModNbtKeys.PORTRAIT_FUR_STAGE, NbtElement.NUMBER_TYPE)
            ? DogFurStage.fromOrdinal(nbt.getInt(ModNbtKeys.PORTRAIT_FUR_STAGE)).ordinal()
            : DogFurStage.NATURAL.ordinal(),
        nbt.getInt(ModNbtKeys.GROWTH_STAGE),
        legacyGenome(nbt),
        nbt.contains(ModNbtKeys.GENES, NbtElement.COMPOUND_TYPE)
            ? DogGenes.fromNbt(nbt.getCompound(ModNbtKeys.GENES))
            : null);
  }

  private static @Nullable DogGenome legacyGenome(final NbtCompound nbt) {
    if (nbt.contains(ModNbtKeys.GENOME, NbtElement.COMPOUND_TYPE)) {
      return DogGenome.fromNbt(nbt.getCompound(ModNbtKeys.GENOME));
    }
    if (!nbt.contains(ModNbtKeys.COMPOSITION, NbtElement.COMPOUND_TYPE)) {
      return null;
    }
    final List<BreedShare> composition =
        DogGenome.compositionFromNbt(nbt.getCompound(ModNbtKeys.COMPOSITION));
    if (composition.isEmpty()) {
      return null;
    }
    final UnleashedDogBreed dominant = DogGenome.dominantOf(composition);
    final UnleashedDogBreed.Attributes attributes = dominant.attributes();
    return new DogGenome(
        composition,
        nbt.getFloat(ModNbtKeys.MAX_HEALTH),
        savedOr(nbt, ModNbtKeys.MOVEMENT_SPEED, attributes.movementSpeed()),
        savedOr(nbt, ModNbtKeys.ATTACK_DAMAGE, attributes.attackDamage()),
        DogGenome.BASELINE_FUR_GROWTH_RATE,
        dominant);
  }

  private static double savedOr(final NbtCompound nbt, final String key, final double fallback) {
    return nbt.contains(key, NbtElement.NUMBER_TYPE) ? nbt.getFloat(key) : fallback;
  }
}
