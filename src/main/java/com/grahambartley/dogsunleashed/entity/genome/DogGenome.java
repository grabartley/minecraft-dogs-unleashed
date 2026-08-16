package com.grahambartley.dogsunleashed.entity.genome;

import com.grahambartley.dogsunleashed.ModNbtKeys;
import com.grahambartley.dogsunleashed.entity.UnleashedDogBreed;
import com.grahambartley.dogsunleashed.pet.BreedComposition.BreedShare;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public record DogGenome(
    List<BreedShare> composition,
    double maxHealth,
    double movementSpeed,
    double attackDamage,
    UnleashedDogBreed voiceBreed) {

  public DogGenome {
    composition = sortedComposition(composition);
  }

  public UnleashedDogBreed dominantBreed() {
    return composition.get(0).breed();
  }

  public static DogGenome pure(final UnleashedDogBreed breed) {
    final UnleashedDogBreed.Attributes attributes = breed.attributes();
    return new DogGenome(
        List.of(new BreedShare(breed, 1.0f)),
        attributes.maxHealth(),
        attributes.movementSpeed(),
        attributes.attackDamage(),
        breed);
  }

  public static List<BreedShare> sortedComposition(final List<BreedShare> composition) {
    if (composition.isEmpty()) {
      throw new IllegalArgumentException("Genome composition must not be empty");
    }
    return composition.stream()
        .sorted(
            Comparator.comparingDouble((BreedShare share) -> -share.share())
                .thenComparing(share -> share.breed().serializedId()))
        .toList();
  }

  public static @Nullable UnleashedDogBreed dominantOf(final List<BreedShare> composition) {
    return composition.isEmpty() ? null : sortedComposition(composition).get(0).breed();
  }

  public static List<BreedShare> compositionOf(final Map<UnleashedDogBreed, Double> shares) {
    return sortedComposition(
        shares.entrySet().stream()
            .map(entry -> new BreedShare(entry.getKey(), entry.getValue().floatValue()))
            .toList());
  }

  public NbtCompound toNbt() {
    final NbtCompound nbt = new NbtCompound();
    nbt.put(ModNbtKeys.GENOME_COMPOSITION, compositionToNbt(this.composition));
    nbt.putDouble(ModNbtKeys.GENOME_MAX_HEALTH, this.maxHealth);
    nbt.putDouble(ModNbtKeys.GENOME_MOVEMENT_SPEED, this.movementSpeed);
    nbt.putDouble(ModNbtKeys.GENOME_ATTACK_DAMAGE, this.attackDamage);
    nbt.putString(ModNbtKeys.GENOME_VOICE_BREED, this.voiceBreed.serializedId());
    return nbt;
  }

  public static @Nullable DogGenome fromNbt(final NbtCompound nbt) {
    final List<BreedShare> composition =
        compositionFromNbt(nbt.getCompound(ModNbtKeys.GENOME_COMPOSITION));
    if (composition.isEmpty()) {
      return null;
    }
    final UnleashedDogBreed voiceBreed =
        UnleashedDogBreed.fromSerializedIdOrNull(nbt.getString(ModNbtKeys.GENOME_VOICE_BREED));
    return new DogGenome(
        composition,
        nbt.getDouble(ModNbtKeys.GENOME_MAX_HEALTH),
        nbt.getDouble(ModNbtKeys.GENOME_MOVEMENT_SPEED),
        nbt.getDouble(ModNbtKeys.GENOME_ATTACK_DAMAGE),
        voiceBreed != null ? voiceBreed : dominantOf(composition));
  }

  public static NbtCompound compositionToNbt(final List<BreedShare> composition) {
    final NbtCompound nbt = new NbtCompound();
    for (final BreedShare share : composition) {
      nbt.putFloat(share.breed().serializedId(), share.share());
    }
    return nbt;
  }

  public static List<BreedShare> compositionFromNbt(final NbtCompound nbt) {
    final List<BreedShare> composition = new ArrayList<>();
    for (final String key : nbt.getKeys()) {
      final UnleashedDogBreed breed = UnleashedDogBreed.fromSerializedIdOrNull(key);
      if (breed != null) {
        composition.add(new BreedShare(breed, nbt.getFloat(key)));
      }
    }
    return composition.isEmpty() ? List.of() : sortedComposition(composition);
  }
}
